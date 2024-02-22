package com.fuse.docx;

import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocException;
import org.docx4j.toc.TocGenerator;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;

import com.fuse.dao.Assessment;
import com.fuse.dao.CustomField;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

public class DocxUtils {
	public String FONT = "";
	private String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	private String[] keywords = { "asmtName", "asmtId", "asmtAppid", "asmtAssessor", "asmtAssessor_Email",
			"asmtAssessor_Lines", "asmtAssessor_Comma", "asmtAssessor_Bullets", "remediation", "asmtTeam", "asmtType",
			"today", "asmtStart", "asmtEnd", "asmtAccessKey" };

	private void checkTableSteps(final WordprocessingMLPackage mlp, List<ExploitStep> steps, String customCSS)
			throws Docx4JException, JAXBException {
		List<Object> tables = getAllElementFromObject(mlp.getMainDocumentPart(), Tbl.class);
		List<Object> paragraphs = getAllElementFromObject(tables, P.class);
		List<Object> cells = new LinkedList<Object>();

		for (Object t : tables) {
			Tbl table = (Tbl) t;
			List<Object> rows = table.getContent();
			for (Object c : rows) {
				if (c.getClass().getName().contains("Tr"))
					cells.addAll(((Tr) c).getContent());
			}
		}
		for (Object c : cells) {
			Object jax = XmlUtils.unwrap(c);
			if (jax.getClass().getName().equals("org.docx4j.wml.Tc")) {
				Tc cell = (Tc) jax;
				int start = -1;
				int index = 0;
				int end = -1;
				for (Object obj : cell.getContent()) {
					String xml = XmlUtils.marshaltoString(obj, false, false);

					if (xml.contains("${exBegin}")) {
						start = index;
					}
					if (xml.contains("${exEnd}")) {
						end = index;
					}
					index++;
				}

				if (start >= 0 && end > 0) {
					List<String> xml = new LinkedList<String>();
					for (int i = start; i <= end; i++) {
						xml.add(XmlUtils.marshaltoString(cell.getContent().get(i)));
					}
					for (int i = end; i >= start; i--) {
						cell.getContent().remove(i);
					}
					index = start;
					int stepNum = 1;
					if(steps.size() == 0) {
						P para  = new P();
						cell.getContent().add(index++, para);
					}else {
						steps = steps.stream().sorted( (s1,s2)-> Long.compare(s1.getStepNum(), s2.getStepNum())).collect(Collectors.toList());
						for (ExploitStep step : steps) {
							for (String nxml : xml) {
								nxml = nxml.replace("${exploit}", "${exploit." + step.getId() + "." + (stepNum) + "}");
								nxml = nxml.replace("${exploitStepNum}", "" + step.getStepNum());
								nxml = nxml.replaceAll("\\$\\{exBegin\\}", "");
								nxml = nxml.replaceAll("\\$\\{exEnd\\}", "");
								cell.getContent().add(index++, XmlUtils.unmarshalString(nxml));

							}
							stepNum++;
						}
					}
					// Forces it to run once per change.
					return;
				}
			}
		}

	}
	
	private boolean cellContains(Tc cell, String variable) {
		for (Object obj : cell.getContent()) {
			String xml = XmlUtils.marshaltoString(obj, false, false);

			if(xml.contains(variable)) {
				return true;
			}
		}
		return false;
	}
	
	private Map<String,BigInteger> setWidths(Tc cell, String variable, Map<String, BigInteger> widths){
		if(cellContains(cell, "${" + variable + "}")) {
			if(cell.getTcPr() != null && cell.getTcPr().getTcW() != null) {
				BigInteger margin = BigInteger.valueOf(200); //TODO: This should not be hardcoded
				widths.put(variable, cell.getTcPr().getTcW().getW().subtract(margin));
			}else {
				widths.put(variable, BigInteger.valueOf(-1));
			}
		}
		return widths;
		
	}

	private void checkTables(final WordprocessingMLPackage mlp, Assessment a, String variable, String customCSS)
			throws JAXBException, Docx4JException {

		List<Object> tables = getAllElementFromObject(mlp.getMainDocumentPart(), Tbl.class);
		for (Object table : tables) {
			List<Object> paragraphs = getAllElementFromObject(table, P.class);
			// This is to get a list of widths to ensure elements in tables behave
			List<Object> cells = getAllElementFromObject(table, Tc.class);
			Map<String, BigInteger> widths = new HashMap<>();
			for(Object cell : cells) {
				Tc tc = (Tc)cell;
				widths = setWidths(tc, "desc", widths);
				widths = setWidths(tc, "rec", widths);
				widths = setWidths(tc, "details", widths);
			}
			String txt = getMatchingText(paragraphs, "${" + variable + "}");
			if (txt == null)
				continue;

			// Found a findings table
			// Get colorsMap if it exits;
			HashMap<String, String> colorMap = new HashMap();
			HashMap<String, String> cellMap = new HashMap();
			String colors = getMatchingText(paragraphs, "${color");
			if (colors != null) {
				colors = colors.replace("${color", "").replace("}", "").trim();
				String[] pairs = colors.split(",");

				for (String pair : pairs) {
					pair = pair.trim();
					colorMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

				}
			}
			colors = getMatchingText(paragraphs, "${cells");
			if (colors != null) {
				colors = colors.replace("${cells", "").replace("}", "").trim();
				String[] pairs = colors.split(",");

				for (String pair : pairs) {
					pair = pair.trim();
					cellMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

				}
			}
			// Now we need to replace data in the rows with vulndata.
			// The loop parameter tells us the row we want to loop though.
			// Loop can also specify the number of rows we want to grab.
			// This way we can repeat more than one row in a table.
			int index = indexOfRow((Tbl) table, paragraphs, "${loop");
			String loop = getMatchingText(paragraphs, "${loop");
			int rowsPlus = 0;
			if (loop != null && loop.contains("-")) {
				loop = loop.split("\\-")[1];
				loop = loop.split("\\}")[0];
				rowsPlus = Integer.parseInt(loop);
			}
			if (index == -1)
				continue;

			List<String> xmls = new LinkedList<>();
			for (int i = 0; i <= rowsPlus; i++) {
				Tr row = (Tr) ((Tbl) table).getContent().get(index + i);
				String xml = XmlUtils.marshaltoString(row, false, false);
				xmls.add(xml);
			}

			// delete our template rows now that we have them stored in strings
			for (int i = rowsPlus; i >= 0; i--) {
				((Tbl) table).getContent().remove(index);
			}

			// now we have a row.. we need to iterate through all issues
			// and replace variables
			int count = 1;
			for (Vulnerability v : a.getVulns()) {
				// Change Colors if need be
				if(v.getSteps() != null) {
					for (ExploitStep ex : v.getSteps()) {
						String desc = ex.getDescription();
						desc = desc.replaceAll("#FAC701", "#" + colorMap.get(v.getOverallStr()));
						desc = desc.replaceAll("#FAC702", "#" + colorMap.get(v.getLikelyhoodStr()));
						desc = desc.replaceAll("#FAC703", "#" + colorMap.get(v.getImpactStr()));
						// desc = desc.replaceAll("#0A0A0A", "#" +colorMap.get(v.getOverallStr()));
						ex.setDescription(desc);

					}
				}
				for (String xml : xmls) {
					String nxml = xml.replaceAll("\\$\\{vulnName\\}", v.getName());
					nxml = nxml.replaceAll("\\$\\{severity\\}", v.getOverallStr());
					nxml = nxml.replaceAll("\\$\\{impact\\}", v.getImpactStr());
					nxml = nxml.replaceAll("\\$\\{cvss\\}", v.getCvssScore());
					nxml = nxml.replaceAll("\\$\\{tracking\\}", v.getTracking());
					try {
						nxml = nxml.replaceAll("\\$\\{vid\\}", "" + v.getId());
					} catch (Exception ex) {
					}
					nxml = nxml.replaceAll("\\$\\{likelihood\\}", v.getLikelyhoodStr());
					nxml = nxml.replaceAll("\\$\\{category\\}",
							v.getCategory() == null ? "UnCategorized" : v.getCategory().getName());
					if (v.getClosed() == null)
						nxml = nxml.replaceAll("\\$\\{status\\}", "Open");
					else
						nxml = nxml.replaceAll("\\$\\{status\\}", "Closed");
					nxml = nxml.replaceAll("\\$\\{count\\}", "" + count);
					nxml = nxml.replaceAll("\\$\\{loop\\}", "");
					nxml = nxml.replaceAll("\\$\\{loop\\-[0-9]+\\}", "");

					if (v.getCustomFields() != null) {
						for (CustomField cf : v.getCustomFields()) {
							nxml = nxml.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
						}
					}

					// change border colors:
					nxml = nxml.replaceAll("w:color=\"FAC701\"", "w:color=\"" + colorMap.get(v.getOverallStr()) + "\"");
					nxml = nxml.replaceAll("w:color=\"FAC702\"",
							"w:color=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
					nxml = nxml.replaceAll("w:color=\"FAC703\"", "w:color=\"" + colorMap.get(v.getImpactStr()) + "\"");

					// Fill Cells
					nxml = nxml.replaceAll("w:fill=\"FAC701\"", "w:fill=\"" + cellMap.get(v.getOverallStr()) + "\"");
					nxml = nxml.replaceAll("w:fill=\"FAC702\"", "w:fill=\"" + cellMap.get(v.getLikelyhoodStr()) + "\"");
					nxml = nxml.replaceAll("w:fill=\"FAC703\"", "w:fill=\"" + cellMap.get(v.getImpactStr()) + "\"");

					// Change font colors:
					nxml = nxml.replaceAll("w:val=\"FAC701\"", "w:val=\"" + colorMap.get(v.getOverallStr()) + "\"");
					nxml = nxml.replaceAll("w:val=\"FAC702\"", "w:val=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
					nxml = nxml.replaceAll("w:val=\"FAC703\"", "w:val=\"" + colorMap.get(v.getImpactStr()) + "\"");
					Tr newrow = (Tr) XmlUtils.unmarshalString(nxml);

					/*
					 * for(String match : cellMap.keySet()) changeColorOfCell(newrow, match,
					 * cellMap.get(match)); for(String match : colorMap.keySet()){ //TODO This
					 * should be deprecated with new method above changeColorOfText(newrow, match,
					 * colorMap.get(match)); }
					 */

					((Tbl) table).getContent().add(newrow);

					HashMap<String, List<Object>> map2 = new HashMap();
					if (xml.contains("${rec}")) {
						if (v.getRecommendation() == null && v.getDefaultVuln() != null) {
							String rec = v.getDefaultVuln().getRecommendation();
							if (v.getCustomFields() != null) {
								for (CustomField cf : v.getCustomFields()) {
									try {
										rec = rec.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}",
												cf.getValue());
									}catch(Exception ex) {
										ex.printStackTrace();
									}
								}
							}
							map2.put("${rec}", wrapHTML(mlp, rec, customCSS, "rec", widths.get("rec")));
						} else if (v.getRecommendation() != null) {
							String rec = v.getRecommendation();
							if (v.getCustomFields() != null) {
								for (CustomField cf : v.getCustomFields()) {
									try {
										rec = rec.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}",
												cf.getValue());
									}catch(Exception ex) {
										ex.printStackTrace();
									}
								}
							}
							map2.put("${rec}", wrapHTML(mlp, rec, customCSS, "rec", widths.get("rec")));
						} else {
							map2.put("${rec}", wrapHTML(mlp, "", customCSS, "rec"));
						}
					}
					if (xml.contains("${desc}")) {
						if (v.getDescription() == null && v.getDefaultVuln() != null) {
							String desc = v.getDefaultVuln().getDescription();
							if (v.getCustomFields() != null) {
								for (CustomField cf : v.getCustomFields()) {
									try {
										desc = desc.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}",
												cf.getValue());
									}catch(Exception ex) {
										ex.printStackTrace();
									}
								}
							}
							map2.put("${desc}", wrapHTML(mlp, desc, customCSS, "desc", widths.get("desc")));
						} else if (v.getDescription() != null) {
							String desc = v.getDescription();
							if (v.getCustomFields() != null) {
								for (CustomField cf : v.getCustomFields()) {
									try {
										desc = desc.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}",
												cf.getValue());
									}catch(Exception ex) {
										ex.printStackTrace();
									}
								}
							}
							map2.put("${desc}", wrapHTML(mlp, desc, customCSS, "desc", widths.get("desc")));
						} else {
							map2.put("${desc}", wrapHTML(mlp, "", customCSS, "desc"));
						}
					}
					if (xml.contains("${details}")) {
						if (v.getDetails() != null) {
							String details = v.getDetails();
							if (v.getCustomFields() != null) {
								for (CustomField cf : v.getCustomFields()) {
									try {
										details = details.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}",
												cf.getValue());
									}catch(Exception ex) {
										ex.printStackTrace();
									}
								}
							}
							map2.put("${details}", wrapHTML(mlp, details, customCSS, "details", widths.get("details")));
						} else {
							map2.put("${details}", wrapHTML(mlp, "", customCSS, "details"));
						}
					}

					replaceHTML(table, map2);

				}

				count++;

			}
			// If no issues are discovered then we just blank out the table.
			if (a.getVulns() == null || a.getVulns().size() == 0) {
				for (String xml : xmls) {
					String nxml = xml.replaceAll("\\$\\{vulnname\\}", "No Issues disclosed.");
					nxml = nxml.replaceAll("\\$\\{severity\\}", "");
					nxml = nxml.replaceAll("\\$\\{impact\\}", "");
					nxml = nxml.replaceAll("\\$\\{cvss\\}", "");
					nxml = nxml.replaceAll("\\$\\{tracking\\}", "");
					try {
						nxml = nxml.replaceAll("\\$\\{vid\\}", "");
					} catch (Exception ex) {
					}
					nxml = nxml.replaceAll("\\$\\{likelihood\\}", "");
					nxml = nxml.replaceAll("\\$\\{category\\}", "");
					nxml = nxml.replaceAll("\\$\\{desc\\}", "");
					nxml = nxml.replaceAll("\\$\\{rec\\}", "");
					nxml = nxml.replaceAll("\\$\\{details\\}", "");
					nxml = nxml.replaceAll("\\$\\{status\\}", "");
					nxml = nxml.replaceAll("\\$\\{count\\}", "1");

					nxml = nxml.replaceAll("\\$\\{loop\\}", "");
					nxml = nxml.replaceAll("\\$\\{loop\\-[0-9]+\\}", "");
					nxml = nxml.replaceAll("\\$\\{cf.*\\}", "");

					Tr newrow = (Tr) XmlUtils.unmarshalString(nxml);
					// update colors
					// TODO bring this back
					for (String match : colorMap.keySet())
						changeColorOfCell(newrow, match, colorMap.get(match));
					for (String match : cellMap.keySet())
						changeColorOfCell(newrow, match, cellMap.get(match));
					for (String match : colorMap.keySet())
						changeColorOfText(newrow, match, colorMap.get(match));
					((Tbl) table).getContent().add(newrow);
				}
			}
			// delete all the variable tables
			int tmpIndex = -1;
			while ((tmpIndex = indexOfRow((Tbl) table, paragraphs, "${")) != -1) {
				((Tbl) table).getContent().remove(tmpIndex);
			}

		}
	}

	public WordprocessingMLPackage generateDocx(final WordprocessingMLPackage mlp, Assessment a, String customCSS)
			throws Exception {

		VariablePrepare.prepare(mlp);

		LinkedList<Vulnerability> vulns = new LinkedList();
		if (a.getVulns() != null && a.getVulns().size() > 0) {
			for (Vulnerability v : a.getVulns()) {
				if (vulns.size() == 0)
					vulns.add(v);
				else {
					for (int i = 0; i < vulns.size(); i++) {
						if (v.getOverall() >= vulns.get(i).getOverall()) {
							vulns.add(i, v);
							break;
						} else if (i == vulns.size() - 1) {
							vulns.add(v);
							break;
						}
					}

				}
			}
		}
		a.setVulns(vulns);

		// Convert all tables and match and replace values
		checkTables(mlp, a, "vulnTable", customCSS);

		// look for findings areass {fiBegin/fiEnd}
		setFindings(mlp, a.getVulns(), customCSS);
		// Update all exploit steps.
		for (Vulnerability v : a.getVulns()) {
			HashMap<String, List<Object>> map2 = new HashMap();
			if(v.getSteps() != null) {
				for (ExploitStep s : v.getSteps()) {
					map2.put("${exploit." + s.getId() + "." + s.getStepNum() + "}",
							wrapHTML(mlp, s.getDescription(), customCSS, "exploit"));
				}
			}
			replaceHTML(mlp.getMainDocumentPart(), map2, false);

		}
		HashMap<String, List<Object>> map = new HashMap();

		map.put("${summary1}",
				this.wrapHTML(mlp, a, a.getSummary() == null ? "" : a.getSummary(), customCSS, "summary1"));
		map.put("${summary2}",
				this.wrapHTML(mlp, a, a.getRiskAnalysis() == null ? "" : a.getRiskAnalysis(), customCSS, "summary2"));
		replaceHTML(mlp.getMainDocumentPart(), map, false);
		replacement(mlp, a, customCSS);

		return mlp;

	}
	

	private List<Object> wrapHTML(final WordprocessingMLPackage mlp, Assessment a, String content, String customCSS,
			String className) throws Docx4JException {
		XHTMLImporterImpl xhtml = new XHTMLImporterImpl(mlp);
		RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
		rfonts.setAscii(this.FONT);

		content = replacement(content, a);
		System.out.println(content);
		return xhtml.convert(
				"<!DOCTYPE html><html><head>"
						+ "<style>html{padding:0;margin:0;margin-right:0px;}\r\nbody{padding:0;margin:0;font-family:"
						+ this.FONT + ";}\r\n" + customCSS + "</style>" + "</head><body><div class='" + className + "'>"
						+ content + "</div></body></html>",
				null);
	}
	private List<Object> wrapHTML(WordprocessingMLPackage mlp, String value, String customCSS, String className) throws Docx4JException{
		return wrapHTML(mlp, value, customCSS, className, BigInteger.valueOf(-1));
	}

	private List<Object> wrapHTML(WordprocessingMLPackage mlp, String value, String customCSS, String className, BigInteger maxWidth)
			throws Docx4JException {
		XHTMLImporterImpl xhtml = new XHTMLImporterImpl(mlp);
		RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
		rfonts.setAscii(this.FONT);
		XHTMLImporterImpl.addFontMapping("Arial", rfonts);
		XHTMLImporterImpl.addFontMapping("arial", rfonts);

		// Fix bad html
		value = FSUtils.jtidy(value);
		try {

			List<Object> converted = xhtml.convert(
							"<!DOCTYPE html><html><head>"
							+ "<style>html{padding:0;margin:0;margin-right:0px;}\r\nbody{padding:0;margin:0;font-family:"
							+ this.FONT + ";}\r\n" + customCSS + "</style>" + "</head><body><div class='" + className
							+ "'>" + value + "</div></body></html>",
					null);
			for (Object o : converted) {
				if (o instanceof P) {
					P p = (P) o;
					/// this is looking for pre elements to ensure they stay inside tables
					if (p.getPPr() != null && p.getPPr().getShd() != null && p.getPPr().getPBdr() != null
							&& p.getPPr().getInd() != null) {
						Ind indent = p.getPPr().getInd();
						BigInteger indValue = indent.getLeft();
						indent.setRight(indValue);
						p.getPPr().setInd(indent);
					}
				}
				
				if(maxWidth.intValue() > -1 && o instanceof Tbl ) {
					Tbl t = (Tbl) o;
					if(t.getTblPr() != null && t.getTblPr().getTblW() != null) {
						t.getTblPr().getTblW().setType("dxa");
						t.getTblPr().getTblW().setW(maxWidth);
					}
				}
			}
			return converted;

		} catch (Exception ex) {

			ex.printStackTrace();
			return null;
		}

	}

	// Replacement for assessment data
	private void replacement(final WordprocessingMLPackage mlp, Assessment a, String customCSS) throws Exception {

		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat("MM/dd/yyyy");

		int[] results = getVulnCount(a);
		String assessors_nl = "";
		String assessors_comma = "";
		String assessors_bullets = "<ul>";
		boolean isfirst = true;
		for (User hacker : a.getAssessor()) {
			assessors_nl += hacker.getFname() + " " + hacker.getLname() + "<br/>";
			assessors_comma += (isfirst ? "" : ", ") + hacker.getFname() + " " + hacker.getLname();
			assessors_bullets += "<li class='bullets'>" + hacker.getFname() + " " + hacker.getLname() + "</li>";
			isfirst = false;
		}
		assessors_bullets += "</ul>";
		HashMap<String, String> map = new HashMap();
		map.put(getKey("asmtname"), a.getName());
		map.put(getKey("asmtid"), "" + a.getId());
		map.put(getKey("asmtappid"), "" + a.getAppId());
		map.put(getKey("asmtassessor"),
				"" + a.getAssessor().get(0).getFname() + " " + a.getAssessor().get(0).getLname());
		map.put(getKey("asmtassessor_email"), "" + a.getAssessor().get(0).getEmail());

		map.put(getKey("asmtassessors_comma"), assessors_comma);

		map.put(getKey("remediation"), "" + a.getRemediation().getFname() + " " + a.getRemediation().getLname());

		map.put(getKey("asmtteam"),
				a.getAssessor() == null ? ""
						: a.getAssessor().get(0).getTeam() == null ? ""
								: a.getAssessor().get(0).getTeam().getTeamName().trim());
		map.put(getKey("asmttype"), a.getType().getType());
		map.put(getKey("today"), formatter.format(new Date()));
		map.put(getKey("asmtstart"), formatter.format(a.getStart()));
		map.put(getKey("asmtend"), formatter.format(a.getEnd()));
		map.put(getKey("asmtaccesskey"), a.getGuid());

		// replavce all cusotm varables
		if (a.getCustomFields() != null) {
			for (CustomField cf : a.getCustomFields()) {
				map.put("cf" + cf.getType().getVariable(), cf.getValue());
			}
		}

		map.putAll(getVulnMap(a));

		replacementText(mlp, map);

		// replace with HTML content
		Map<String, List<Object>> map2 = new HashMap();
		map2.put("${asmtAssessors_Lines}", wrapHTML(mlp, assessors_nl, customCSS, ""));
		map2.put("${asmtAssessors_Bullets}", wrapHTML(mlp, assessors_bullets, customCSS, ""));
		map2.put("${asmtAssessors_Comma}", wrapHTML(mlp, assessors_comma, customCSS, ""));
		replaceHTML(mlp.getMainDocumentPart(), map2);

	}

	private String loopReplace(String content, Assessment a) {
		content = innerLoop(content, a, 5);
		content = innerLoop(content, a, 4);
		content = innerLoop(content, a, 3);
		content = innerLoop(content, a, 2);
		content = innerLoop(content, a, 1);
		content = innerLoop(content, a, 0);
		return content;

	}

	private String innerLoop(String content, Assessment a, int rank) {
		Vulnerability tmp = new Vulnerability();
		String Var = tmp.vulnStr(new Long(rank)).toUpperCase();
		if (content.contains("{[asmt" + Var + "]}")) {
			String html = "<ol>\r\n";
			boolean isSomething = false;
			for (Vulnerability v : a.getVulns()) {
				if (v.getOverall() == rank) {
					isSomething = true;
					html += "<li>" + v.getName() + "</li>";
				}
			}
			html += "</ol>";
			if (isSomething == false) {
				html = "<i>No vulnerabilities found at this severity.</i>&nbsp;";
			}
			content = content.replaceAll("\\{\\[assessment\\." + Var + "\\]\\}", html);
		}
		return content;
	}

	private int[] getVulnCount(Assessment a) {
		// C,H,M,L,I,R
		int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		// 9,8,7,6,5,4,3,2,1,0
		if (a.getVulns() == null)
			return results;
		else {
			for (Vulnerability v : a.getVulns()) {
				if (v.getOverall() == null || v.getOverall() == -1l)
					continue;

				results[v.getOverall().intValue()]++;
			}
			return results;
		}

	}

	private String getVulnCount(String content, Assessment a) {
		// C,H,M,L,I,R
		int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		// 9,8,7,6,5,4,3,2,1,0
		int totals = 0;
		if (a.getVulns() != null) {
			for (Vulnerability v : a.getVulns()) {
				if (v.getOverall() == null || v.getOverall() == -1l)
					continue;
				results[v.getOverall().intValue()]++;
				totals++;
			}
			for (int i = 0; i < 10; i++) {
				content = content.replaceAll("\\$\\{riskCount" + i + "\\}", "" + results[i]);
			}
			content = content.replaceAll("\\$\\{riskTotal\\}", "" + totals);
		}
		return content;
	}

	private Map<String, String> getVulnMap(Assessment a) {
		Map<String, String> maps = new HashMap();
		// C,H,M,L,I,R
		int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		// 9,8,7,6,5,4,3,2,1,0
		int totals = 0;
		if (a.getVulns() != null) {
			for (Vulnerability v : a.getVulns()) {
				if (v.getOverall() == null || v.getOverall() == -1l)
					continue;
				results[v.getOverall().intValue()]++;
				totals++;
			}
		}
		for (int i = 0; i < 10; i++) {
			maps.put("riskCount" + i + "", "" + results[i]);

		}
		maps.put("riskTotal", "" + totals);
		return maps;
	}

	private Map<String, List<Object>> getVulnMap(WordprocessingMLPackage mlp, Assessment a, String customCSS)
			throws Docx4JException {
		Map<String, List<Object>> maps = new HashMap();
		// C,H,M,L,I,R
		int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		// 9,8,7,6,5,4,3,2,1,0
		if (a.getVulns() != null) {
			for (Vulnerability v : a.getVulns()) {
				if (v.getOverall() == null || v.getOverall() == -1l)
					continue;
				results[v.getOverall().intValue()]++;
			}
		}
		for (int i = 0; i < 10; i++) {
			maps.put("${riskCount" + i + "}", wrapHTML(mlp, "" + results[i], customCSS, ""));

		}
		return maps;
	}

	private String getKey(String key) {
		for (String word : keywords) {
			word = word.replace("\\", "");
			if (word.toLowerCase().equals(key.toLowerCase()))
				return word;
		}
		return "assessment.nothing";
	}

	// Replacement for vulns
	private void replacement(final WordprocessingMLPackage mlp, Vulnerability v, String customCSS)
			throws Docx4JException, JAXBException {
		HashMap<String, String> map = new HashMap();
		map.put("vulName", v.getName());
		map.put("severity", v.getOverallStr());
		map.put("likelihood", v.getLikelyhoodStr());
		map.put("impact", v.getImpactStr());
		try {
			map.put("vid", "" + v.getId());
		} catch (Exception ex) {
		}
		map.put("tracking", "" + v.getTracking());
		map.put("cvss", "" + v.getCvssScore());

		HashMap<String, List<Object>> map2 = new HashMap();

		if (v.getDescription() == null && v.getDefaultVuln() != null) {
			map2.put("${desc}", wrapHTML(mlp, v.getDefaultVuln().getDescription(), customCSS, "desc"));
		} else if (v.getDescription() != null) {
			map2.put("${desc}", wrapHTML(mlp, v.getDescription(), customCSS, "desc"));
		} else {
			map2.put("${desc}", wrapHTML(mlp, "", customCSS, "desc"));
		}
		if (v.getRecommendation() == null && v.getDefaultVuln() != null) {
			map2.put("${rec}", wrapHTML(mlp, v.getDefaultVuln().getRecommendation(), customCSS, "rec"));
		} else if (v.getRecommendation() != null) {
			map2.put("${rec}", wrapHTML(mlp, v.getRecommendation(), customCSS, "rec"));
		} else {
			map2.put("${rec}", wrapHTML(mlp, "", customCSS, "rec"));
		}
		
		if (v.getDetails() != null) {
			map2.put("${details}", wrapHTML(mlp, v.getDetails(), customCSS, "details"));
		} else {
			map2.put("${details}", wrapHTML(mlp, "", customCSS, "details"));
		}

		replacementText(mlp, map);
		replaceHTML(mlp.getMainDocumentPart(), map2);

	}

	// Replacement for Exploit Steps
	private void replacement(final WordprocessingMLPackage mlp, ExploitStep s, String customCSS) throws Exception {
		Map<String, List<Object>> map = new HashMap();
		map.put("${exploit}", wrapHTML(mlp, s.getDescription(), customCSS, "exploit"));
		replaceHTML(mlp.getMainDocumentPart(), map);
	}

	// Replace simple text
	private void replacementText(final WordprocessingMLPackage mlp, Map<String, String> map)
			throws JAXBException, Docx4JException {
		String xml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), false, false);
		for (String key : map.keySet()) {
			xml = xml.replaceAll("\\$\\{" + key + "\\}", map.get(key) == null ? "" : "<![CDATA[" +map.get(key) + "]]>");
		}
		mlp.getMainDocumentPart().setContents((org.docx4j.wml.Document) XmlUtils.unmarshalString(xml));

	}

	// replace text/html content
	private String replacement(String content, Assessment a) {
		SimpleDateFormat formatter;

		formatter = new SimpleDateFormat("MM/dd/yyyy");

		String assessors_nl = "";
		String assessors_comma = "";
		String assessors_bullets = "<ul>";
		boolean isfirst = true;
		for (User hacker : a.getAssessor()) {
			assessors_nl += hacker.getFname() + " " + hacker.getLname() + "<br/>";
			assessors_comma += (isfirst ? "" : ", ") + hacker.getFname() + " " + hacker.getLname();
			assessors_bullets += "<li class='bullets'>" + hacker.getFname() + " " + hacker.getLname() + "</li>";
			isfirst = false;
		}
		assessors_bullets += "</ul>";

		content = content.replaceAll("\\$\\{asmtName\\}", a.getName() == null ? "" : a.getName());
		content = content.replaceAll("\\$\\{asmtId\\}", a.getId() == null ? "" : "" + a.getId());
		content = content.replaceAll("\\$\\{asmtAppId\\}", "" + a.getAppId());
		content = content.replaceAll("\\$\\{asmtAssessor\\}", a.getAssessor() == null ? ""
				: (a.getAssessor().get(0).getFname() + " " + a.getAssessor().get(0).getLname()));
		content = content.replaceAll("\\$\\{asmtAssessor_Email\\}",
				a.getAssessor() == null ? "" : (a.getAssessor().get(0).getEmail()));
		content = content.replaceAll("\\$\\{asmtAssessors_Lines\\}", a.getAssessor() == null ? "" : assessors_nl);
		content = content.replaceAll("\\$\\{asmtAssessors_Comma\\}", a.getAssessor() == null ? "" : assessors_comma);
		content = content.replaceAll("\\$\\{asmtAssessors_Bullets\\}",
				a.getAssessor() == null ? "" : assessors_bullets);
		content = content.replaceAll("\\$\\{remediation\\}", a.getRemediation() == null ? ""
				: (a.getRemediation().getFname() + " " + a.getRemediation().getLname()));
		content = getVulnCount(content, a);

		content = content.replaceAll("\\$\\{asmteam\\}",
				a.getAssessor() == null ? ""
						: a.getAssessor().get(0).getTeam() == null ? ""
								: a.getAssessor().get(0).getTeam().getTeamName().trim());
		content = content.replaceAll("\\$\\{asmtType\\}", a.getType() == null ? "" : a.getType().getType().trim());
		content = content.replaceAll("\\$\\{today\\}", formatter.format(new Date()));
		content = content.replaceAll("\\$\\{asmtStandND\\}", formatter.format(a.getEnd()));
		content = content.replaceAll("\\$\\{asmtAccessKey\\}", a.getGuid());
		content = loopReplace(content, a);

		// Fix any html weirdness
		content = FSUtils.jtidy(content);
		return content;

	}

	private void setFindings(final WordprocessingMLPackage mlp, List<Vulnerability> vulns, String customCSS)
			throws JAXBException, Docx4JException {
		int begin = getIndex(mlp.getMainDocumentPart(), "${fiBegin}");
		int end = getIndex(mlp.getMainDocumentPart(), "${fiEnd}");
		if (begin == -1)
			return;
		if (end == -1)
			return;

		HashMap<String, String> colorMap = new HashMap();

		// Get relevent parts of the document and put them into a
		// temporary array.
		List<Object> findingTemplate = new LinkedList();
		for (int i = begin; i <= end; i++) {
			findingTemplate.add(mlp.getMainDocumentPart().getContent().get(i));
			List<Object> paragraphs = getAllElementFromObject(mlp.getMainDocumentPart().getContent().get(i), P.class);
			String colors = getMatchingText(paragraphs, "${color");
			if (colors != null) {
				colors = colors.replace("${color", "").replace("}", "").trim();
				String[] pairs = colors.split(",");

				for (String pair : pairs) {
					pair = pair.trim();
					colorMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

				}
			}
		}
		// Remove the elements from the doc. These will be replaced with
		// out temp array when its updated later on
		for (int i = end; i >= begin; i--) {
			mlp.getMainDocumentPart().getContent().remove(i);
		}

		for (Vulnerability v : vulns) {
			for (Object obj : findingTemplate) {

				String xml = XmlUtils.marshaltoString(obj, false, false);
				String nxml = xml.replaceAll("\\$\\{vulnname\\}", v.getName());
				// nxml = nxml.replaceAll("\\$\\{vulnerability.NAME\\}", v.getName());
				nxml = nxml.replaceAll("\\$\\{severity\\}", v.getOverallStr());
				// nxml = nxml.replaceAll("\\$\\{vulnerability.SEVERITY\\}", v.getOverallStr());
				nxml = nxml.replaceAll("\\$\\{impact\\}", v.getImpactStr());
				// nxml = nxml.replaceAll("\\$\\{vulnerability.IMPACT\\}", v.getImpactStr());
				nxml = nxml.replaceAll("\\$\\{cvss\\}", v.getCvssScore());
				// nxml = nxml.replaceAll("\\$\\{vulnerability.CVSS\\}", v.getCvssScore());
				nxml = nxml.replaceAll("\\$\\{tracking\\}", v.getTracking());
				// nxml = nxml.replaceAll("\\$\\{vulnerability.TRACKING\\}", v.getTracking());
				try {
					nxml = nxml.replaceAll("\\$\\{vid\\}", "" + v.getId());
					// nxml = nxml.replaceAll("\\$\\{vulnerability.ID\\}", ""+v.getId());
				} catch (Exception ex) {
				}
				nxml = nxml.replaceAll("\\$\\{likelihood\\}", v.getLikelyhoodStr());
				// nxml = nxml.replaceAll("\\$\\{vulnerability.LIKELIHOOD\\}",
				// v.getLikelyhoodStr());
				nxml = nxml.replaceAll("\\$\\{category\\}",
						v.getCategory() == null ? "UnCategorized" : v.getCategory().getName());
				if (v.getClosed() == null) {
					nxml = nxml.replaceAll("\\$\\{status\\}", "Open");
					// nxml = nxml.replaceAll("\\$\\{vulnerability.STATUS\\}", "Open");
				} else {
					nxml = nxml.replaceAll("\\$\\{status\\}", "Closed");
					// nxml = nxml.replaceAll("\\$\\{vulnerability.STATUS\\}", "Closed");
				}
				// remove color loops
				nxml = nxml.replaceAll("\\$\\{color.*\\}", "");

				if (v.getCustomFields() != null) {
					for (CustomField cf : v.getCustomFields()) {
						nxml = nxml.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
					}
				}

				nxml = nxml.replaceAll("w:val=\"FAC701\"", "w:val=\"" + colorMap.get(v.getOverallStr()) + "\"");
				nxml = nxml.replaceAll("w:val=\"FAC702\"", "w:val=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
				nxml = nxml.replaceAll("w:val=\"FAC703\"", "w:val=\"" + colorMap.get(v.getImpactStr()) + "\"");

				Object paragraph = XmlUtils.unmarshalString(nxml);

				mlp.getMainDocumentPart().getContent().add(begin++, paragraph);

			}
		}

		// Add the vulnerability description and recommandations.
		// update custom fields inside the html before inserting into the document.
		for (Vulnerability v : vulns) {
			HashMap<String, List<Object>> map2 = new HashMap();

			if (v.getDescription() == null && v.getDefaultVuln() != null) {
				String desc = v.getDefaultVuln().getDescription();
				if (v.getCustomFields() != null) {
					for (CustomField cf : v.getCustomFields()) {
						desc = desc.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
					}
				}
				map2.put("${desc}", wrapHTML(mlp, desc, customCSS, "desc"));
			} else if (v.getDescription() != null) {
				String desc = v.getDescription();
				if (v.getCustomFields() != null) {
					for (CustomField cf : v.getCustomFields()) {
						desc = desc.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
					}
				}
				map2.put("${desc}", wrapHTML(mlp, desc, customCSS, "desc"));
			} else {
				map2.put("${desc}", wrapHTML(mlp, "", customCSS, "desc"));
			}
			if (v.getRecommendation() == null && v.getDefaultVuln() != null) {
				String rec = v.getDefaultVuln().getRecommendation();
				if (v.getCustomFields() != null) {
					for (CustomField cf : v.getCustomFields()) {
						rec = rec.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
					}
				}
				map2.put("${rec}", wrapHTML(mlp, rec, customCSS, "rec"));
			} else if (v.getRecommendation() != null) {
				String rec = v.getRecommendation();
				if (v.getCustomFields() != null) {
					for (CustomField cf : v.getCustomFields()) {
						rec = rec.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
					}
				}
				map2.put("${rec}", wrapHTML(mlp, rec, customCSS, "rec"));
			} else {
				map2.put("${rec}", wrapHTML(mlp, "", customCSS, "rec"));
			}
			if (v.getDetails() != null) {
				String details = v.getDetails();
				if (v.getCustomFields() != null) {
					for (CustomField cf : v.getCustomFields()) {
						details = details.replaceAll("\\$\\{cf" + cf.getType().getVariable() + "\\}", cf.getValue());
					}
				}
				map2.put("${details}", wrapHTML(mlp, details, customCSS, "details"));
			} else {
				map2.put("${details}", wrapHTML(mlp, "", customCSS, "details"));
			}
			replaceHTML(mlp.getMainDocumentPart(), map2, true);

			HashMap<String, String> map1 = new HashMap();
			if (v.getCustomFields() != null) {
				for (CustomField cf : v.getCustomFields()) {
					map1.put("cf" + cf.getType().getVariable(), cf.getValue());
				}
			}

			replacementText(mlp, map1);
		}

	}

	private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
		List<Object> result = new ArrayList<Object>();
		if (obj instanceof JAXBElement)
			obj = ((JAXBElement<?>) obj).getValue();

		if (obj.getClass().equals(toSearch))
			result.add(obj);
		else if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}

		}
		return result;
	}

	/*
	 * Change color of a table cell based on the variable names
	 */
	private int changeColorOfCell(Tr row, String variable, String color) {
		List<Object> paragraphs = getAllElementFromObject(row, P.class);
		for (Object para : paragraphs) {
			if (matchText((P) para, variable)) {
				Tc cell = ((Tc) ((P) para).getParent());
				if (cell.getTcPr().getShd() != null) {
					cell.getTcPr().getShd().setFill(color);
				} else {
					CTShd shader = new CTShd();
					shader.setColor("auto");
					shader.setFill(color);

					cell.getTcPr().setShd(shader);
				}
			}
		}
		return -1;
	}

	private int changeColorOfText(Tr row, String variable, String color) {
		List<Object> paragraphs = getAllElementFromObject(row, P.class);
		for (Object para : paragraphs) {
			if (matchText((P) para, variable)) {
				CTShd shader = new CTShd();
				shader.setColor(color);
				for (Object o : ((P) para).getContent()) {
					if (o.getClass().getName().equals("org.docx4j.wml.R")) {
						BooleanDefaultTrue setBold = new BooleanDefaultTrue();
						setBold.setVal(false);
						BooleanDefaultTrue setI = new BooleanDefaultTrue();
						setI.setVal(false);
						if (((R) o).getRPr() != null) {
							if (((R) o).getRPr().getB() != null && ((R) o).getRPr().getB().isVal()) {
								setBold.setVal(true);
							}
							if (((R) o).getRPr().getI() != null && ((R) o).getRPr().getI().isVal()) {
								setI.setVal(true);
							}

						}
						org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
						org.docx4j.wml.RPr rpr = factory.createRPr();
						org.docx4j.wml.Color colr = factory.createColor();
						colr.setVal(color);
						rpr.setColor(colr);
						rpr.setB(setBold);
						rpr.setI(setI);
						((R) o).setRPr(rpr);
					}
				}
			}
		}
		return -1;
	}

	/*
	 * Utiltity function to file elements in the docx file
	 */
	private int indexOfRow(Tbl table, List<Object> paragraphs, String variable) {
		for (Object para : paragraphs) {
			if (matchText((P) para, variable)) {

				Tc cell = ((Tc) ((P) para).getParent());
				if (cell.getParent().getClass().getName().equals("org.docx4j.wml.Tr")) {
					return table.getContent().indexOf((Tr) cell.getParent());
				} else {
					JAXBElement jrow = ((JAXBElement) cell.getParent());
					List<Object> rows = table.getContent();
					for (Object oRow : rows) {
						Tr row = (Tr) oRow;
						if (row.getContent().indexOf(jrow) >= 0) {
							return table.getContent().indexOf(row);
						}
					}
				}

			}
		}
		return -1;
	}

	private int indexOfCell(Tbl table, List<Object> paragraphs, String variable) {
		for (Object para : paragraphs) {
			if (matchText((P) para, variable)) {

				Tc cell = ((Tc) ((P) para).getParent());
				if (cell.getParent().getClass().getName().equals("org.docx4j.wml.Tr")) {
					return ((Tr) cell.getParent()).getContent().indexOf(cell);
				}
			}
		}
		return -1;
	}

	/*
	 * Check if there is a match in the text
	 */
	private boolean matchText(P paragraph, String variable) {
		final StringWriter paragraphText = new StringWriter();
		try {
			TextUtils.extractText(paragraph, paragraphText);
		} catch (Exception ex) {
			return false;
		}
		final String identifier = paragraphText.toString();
		if (identifier != null && identifier.startsWith(variable)) {
			return true;
		}
		return false;
	}

	private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements) {
		replaceHTML(mainPart, replacements, true);
	}

	private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements, boolean once) {
		Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
		Preconditions.checkNotNull(replacements, "replacements may not be null!");

		// look for all P elements in the specified object
		final List<P> paragraphs = Lists.newArrayList();
		new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) {
					paragraphs.add((P) o);
				}

				return null;
			}
		});

		// run through all found paragraphs to located identifiers
		for (final P paragraph : paragraphs) {
			// check if this is one of our identifiers
			final StringWriter paragraphText = new StringWriter();
			try {
				TextUtils.extractText(paragraph, paragraphText);
			} catch (Exception ex) {

			}

			final String identifier = paragraphText.toString();
			if (identifier != null && replacements.containsKey(identifier)) {
				final List<Object> listToModify;

				if (paragraph.getParent() instanceof Tc) {
					// paragraph located in table-cell
					final Tc parent = (Tc) paragraph.getParent();
					listToModify = parent.getContent();
				} else if (paragraph.getParent() instanceof Hdr) {
					final Hdr parent = (Hdr) paragraph.getParent();
					listToModify = parent.getContent();
				} else {
					// paragraph located in main document part
					listToModify = ((MainDocumentPart) mainPart).getContent();
				}

				if (listToModify != null) {
					final int index = listToModify.indexOf(paragraph);
					Preconditions.checkState(index > -1, "could not located the paragraph in the specified list!");

					// remove the paragraph from it's current index
					listToModify.remove(index);

					// add the converted HTML paragraphs
					listToModify.addAll(index, replacements.get(identifier));
					if (once)
						replacements.remove(identifier);

				}

			}
		}
	}

	public void tocGenerator(WordprocessingMLPackage mlp) {
		try {
			// Generate the table of contents
			TocGenerator tocGenerator = new TocGenerator(mlp);
			// tocGenerator.pageNumbersViaXSLT(true);
			int index = this.getIndex(mlp.getMainDocumentPart(), "${TOC}");
			if (index == -1)
				return;
			// Toc.setTocHeadingText("");
			tocGenerator.generateToc(index, "TOC \\o \"1-3\" \\h \\z \\u ", STTabTlc.DOT, true);
			addPageBreak(mlp, index + 1);
		} catch (TocException e) {
			e.printStackTrace();
		}

	}

	private void addPageBreak(WordprocessingMLPackage mlp, int index) {
		org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
		P p = wmlObjectFactory.createP();
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for br
		Br br = wmlObjectFactory.createBr();
		r.getContent().add(br);
		br.setType(org.docx4j.wml.STBrType.PAGE);
		mlp.getMainDocumentPart().getContent().add(index, p);

	}

	private int getIndex(final MainDocumentPart mainPart, String keyword) {
		Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");

		// look for all P elements in the specified object
		final List<P> paragraphs = Lists.newArrayList();
		new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) {
					paragraphs.add((P) o);
				}

				return null;
			}
		});

		// run through all found paragraphs to located identifiers
		for (final P paragraph : paragraphs) {
			// check if this is one of our identifiers
			final StringWriter paragraphText = new StringWriter();
			try {
				TextUtils.extractText(paragraph, paragraphText);
			} catch (Exception ex) {

			}

			final String identifier = paragraphText.toString();
			if (identifier != null && identifier.contains(keyword)) {
				int index = mainPart.getContent().indexOf(paragraph);
				if (index == -1) {
					return -1;
				} else {
					mainPart.getContent().remove(index);
					// mainPart.getContent().set(index, new P());
					return index;
				}

			}

		}
		return -1;
	}

	private String getMatchingText(P paragraph, String variable) {
		final StringWriter paragraphText = new StringWriter();
		try {
			TextUtils.extractText(paragraph, paragraphText);
		} catch (Exception ex) {
			return null;
		}
		final String identifier = paragraphText.toString();
		if (identifier != null && identifier.startsWith(variable)) {
			return identifier;
		}
		return null;
	}

	private String getMatchingText(List<Object> paragraphs, String variable) {
		final StringWriter paragraphText = new StringWriter();
		for (Object paragraph : paragraphs) {
			String text = getMatchingText((P) paragraph, variable);
			if (text != null)
				return text;
		}
		return null;
	}

}
