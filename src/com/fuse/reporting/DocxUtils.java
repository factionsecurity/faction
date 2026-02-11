package com.fuse.reporting;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.toc.TocException;
import org.docx4j.toc.TocGenerator;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase.Ind;
import org.dom4j.CDATA;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import com.faction.reporting.ReportFeatures;
import com.fuse.dao.Assessment;
import com.fuse.dao.CustomField;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.extenderapi.Extensions;
import com.fuse.utils.FSUtils;
import com.fuse.utils.ImageBorderUtil;
import com.fuse.utils.LoggingConfig;
import com.fuse.utils.MethodProfiler;
import com.fuse.utils.ProfileMethod;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

public class DocxUtils {
    public String FONT = "";
    private String[] keywords = { "asmtName", "asmtId", "asmtAppid", "asmtAssessor", "asmtAssessor_Email",
            "asmtAssessor_Lines", "asmtAssessor_Comma", "asmtAssessor_Bullets", "remediation", "asmtTeam", "asmtType",
            "today", "asmtStart", "asmtEnd", "asmtAccessKey", "totalOpenVulns", "totalClosedVulns" };
    private Extensions reportExtension;
    private Assessment assessment;
    private List<Vulnerability> vulns;
    private final WordprocessingMLPackage mlp;
    private String[] reportSections = new String[0];
    private HashMap<String, List<Object>> nodeMap = new HashMap<>();

    public DocxUtils(EntityManagerFactory entityManagerFactory, WordprocessingMLPackage mlp, Assessment assessment) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "DocxUtils");
        try {
            this.mlp = mlp;
            this.reportExtension = new Extensions(entityManagerFactory, Extensions.EventType.REPORT_MANAGER);
            this.assessment = assessment;
            //this.outlineImages();
            this.vulns = assessment.getVulns();
            this.setupReportSections(entityManagerFactory);
        } finally {
            context.end();
        }
    }

    public DocxUtils(WordprocessingMLPackage mlp, Assessment assessment) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "DocxUtils");
        try {
            this.mlp = mlp;
            this.reportExtension = new Extensions(HibHelper.getInstance().getEMF(),
                    Extensions.EventType.REPORT_MANAGER);
            this.assessment = assessment;
            // this.outlineImages();
            this.vulns = assessment.getVulns();
            this.setupReportSections(HibHelper.getInstance().getEMF());
        } finally {
            context.end();
        }
    }

    private void setupReportSections(EntityManagerFactory entityManagerFactory) {
        if (ReportFeatures.allowSections()) {
            EntityManager em = entityManagerFactory.createEntityManager();
            try {
                SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
                        .findFirst().orElse(null);
                String features = ems.getFeatures();
                reportSections = ReportFeatures.getFeatures(features);
            } finally {
                em.close();
            }
        }
    }

    private Boolean sectionExists(String section) {
        return Arrays.asList(this.reportSections).stream().anyMatch(reportSection -> reportSection.equals(section));
    }

    private boolean cellContains(Tc cell, String variable) {
        for (Object obj : cell.getContent()) {
            String xml = XmlUtils.marshaltoString(obj, false, false);

            if (xml.contains(variable)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, BigInteger> setWidths(Tc cell, String variable, Map<String, BigInteger> widths) {
        if (cellContains(cell, "${" + variable + "}")) {
            if (cell.getTcPr() != null && cell.getTcPr().getTcW() != null) {
                BigInteger margin = BigInteger.valueOf(200); // TODO: This should not be hardcoded
                widths.put(variable, cell.getTcPr().getTcW().getW().subtract(margin));
            } else {
                widths.put(variable, BigInteger.valueOf(-1));
            }
        }
        return widths;

    }

    @ProfileMethod("Search and Sort Vulns")
    private List<Vulnerability> getFilteredVulns(String section) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getFilteredVulns");
        try {

			if (section == null) {
				section = "Default";
			} else if (section.isEmpty()) {
				section = "Default";
			}

			List<Vulnerability> filteredVulns = new ArrayList<>();
			if (section.equals("Default")) {
				filteredVulns = this.vulns.stream()
						.filter(vuln -> vuln.getSection() == null
								|| vuln.getSection().isEmpty()
								|| vuln.getSection().equals("Default")
								|| !sectionExists(vuln.getSection()))
						.collect(Collectors.toList());
			} else {
				final String query = section;
				filteredVulns = this.vulns.stream()
						.filter(
								vuln -> vuln.getSection().equals(query))
						.collect(Collectors.toList());
			}

			return filteredVulns;
        }finally {
        	context.end();
    	}
    }

    private static String CData(String text) {
        return "<![CDATA[" + text + "]]>";
    }

    @ProfileMethod("Process vulnerability tables and populate with data")
    private void checkTables(String variable, String section, String customCSS)
            throws JAXBException, Docx4JException {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "checkTables");
        try {
            List<Vulnerability> filteredVulns = this.getFilteredVulns(section);

            List<Object> tables = getAllElementFromObject(mlp.getMainDocumentPart(), Tbl.class);
            System.out.println("Num Tables: " + tables.size());
            for (Object table : tables) {
            	MethodProfiler.ProfileContext context2 = MethodProfiler.start("DocxUtils", "sectionTest");
                List<Object> paragraphs = getAllElementFromObject(table, P.class);
                System.out.println("Num Paragraphs: " + paragraphs.size());
                // This is to get a list of widths to ensure elements in tables behave
                List<Object> cells = getAllElementFromObject(table, Tc.class);
                System.out.println("Num Cells: " + cells.size());
                Map<String, BigInteger> widths = new HashMap<>();
                for (Object cell : cells) {
                    Tc tc = (Tc) cell;
                    widths = setWidths(tc, "desc", widths);
                    widths = setWidths(tc, "rec", widths);
                    widths = setWidths(tc, "details", widths);
                }
                String tableVariable = "${" + variable + "}";
                if (ReportFeatures.allowSections() && section != null && !section.isEmpty()
                        && !section.equals("Default")) {
                    tableVariable = "${" + variable + " " + section + "}";
                }
                String txt = getMatchingText(paragraphs, tableVariable);
                if (txt == null)
                    continue;

                // Found a findings table
                // Get colorsMap if it exits;
                HashMap<String, String> colorMap = new HashMap<>();
                HashMap<String, String> cellMap = new HashMap<>();
                HashMap<String, String> customFieldMap = new HashMap<>();
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

                String customFields = getMatchingText(paragraphs, "${custom-fields");
                if (customFields != null) {
                    customFields = customFields.replace("${custom-fields", "").replace("}", "").trim();
                    String[] pairs = customFields.split(",");

                    for (String pair : pairs) {
                        pair = pair.trim();
                        customFieldMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

                    }
                }
                String noIssuesText = getMatchingText(paragraphs, "${noIssuesText");
                if (noIssuesText != null) {
                    noIssuesText = noIssuesText.replace("${noIssuesText ", "").replace("}", "");
                } else {
                    noIssuesText = "No issues detected for this section.";
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
                // Create an XML array or table rows
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
                context2.end();
                // now we have a row.. we need to iterate through all issues
                // and replace variables
                SimpleDateFormat formatter;
                formatter = new SimpleDateFormat("MM/dd/yyyy");
                int count = 1;
                int sevIndex = 0;
                String prevSev = "";
                for (Vulnerability v : filteredVulns) {
                    if (prevSev == v.getOverallStr()) {
                        sevIndex++;
                    } else {
                        prevSev = v.getOverallStr();
                        sevIndex = 1;
                    }
                    // Change Colors if need be
                    for (String xml : xmls) {
                    	String nxml = replaceXml(xml, v, customFieldMap, colorMap, cellMap, null, count, sevIndex); 
                        Tr newrow = (Tr) XmlUtils.unmarshalString(nxml);

                        // Replace Hyperlinks
                        if (v.getCustomFields() != null) {
                            for (CustomField cf : v.getCustomFields()) {
                                this.replaceHyperlink(newrow, "${cf" + cf.getType().getVariable() + " link}",
                                        cf.getValue());
                            }
                        }
                        this.replaceHyperlink(newrow, "${cvssString link}", v.getCvssString());

                        /*
                         * for(String match : cellMap.keySet()) changeColorOfCell(newrow, match,
                         * cellMap.get(match)); for(String match : colorMap.keySet()){ //TODO This
                         * should be deprecated with new method above changeColorOfText(newrow, match,
                         * colorMap.get(match)); }
                         */

                        HashMap<String, List<Object>> detailsMap = new HashMap<>();
                        if (xml.contains("${rec}")) {
                        	String rec = getRecommendation(v);
							rec = this.replaceFigureVariables(rec, count);
							detailsMap.put("${rec}", wrapHTML(rec, customCSS, "rec"));
                        }
                        if (xml.contains("${desc}")) {
                        	String desc = getDescription(v);
							desc = this.replaceFigureVariables(desc, count);
							detailsMap.put("${desc}", wrapHTML(desc, customCSS, "desc"));
                        }
                        if (xml.contains("${details}")) {
                        	String details = getDetails(v);
							details = this.replaceFigureVariables(details, count);
							detailsMap.put("${details}", wrapHTML(details, customCSS, "details"));
                        }

                        if (v.getCustomFields() != null) {
                            for (CustomField cf : v.getCustomFields()) {
                                if (cf.getType().getFieldType() == 3) {
                                    detailsMap.put("${cf" + cf.getType().getVariable() + "}",
                                            wrapHTML(cf.getValue(), customCSS, cf.getType().getVariable()));
                                }
                            }
                        }
                        replaceHTML(newrow, detailsMap);
                        ((Tbl) table).getContent().add(newrow);
                    }
                    count++;
                }
                // If no issues are discovered then we just blank out the table.
                if (filteredVulns == null || filteredVulns.size() == 0) {
                    ObjectFactory factory = Context.getWmlObjectFactory();
                    Tr newrow = factory.createTr();
                    Tc td = factory.createTc();
                    P p = factory.createP();
                    R r = factory.createR();
                    Text text = factory.createText();
                    text.setValue(noIssuesText);
                    r.getContent().add(text);
                    p.getContent().add(r);
                    td.getContent().add(p);
                    newrow.getContent().add(td);
                    ((Tbl) table).getContent().add(newrow);
                }
                // delete all the variable tables
                int tmpIndex = -1;
                while ((tmpIndex = indexOfRow((Tbl) table, paragraphs, "${")) != -1) {
                    ((Tbl) table).getContent().remove(tmpIndex);
                }

            }
        } finally {
            context.end();
        }
    }

    @ProfileMethod("Main document generation entry point with custom CSS")
    public WordprocessingMLPackage generateDocx(String customCSS)
            throws Exception {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "generateDocx");
        try {
            VariablePrepare.prepare(mlp);

            // Convert all tables and match and replace values
            checkTables("vulnTable", "Default", customCSS);
            // look for findings areas {fiBegin/fiEnd}
            setFindings("Default", customCSS);
            if (ReportFeatures.allowSections()) {
                for (String section : this.reportSections) {
                    checkTables("vulnTable", section, customCSS);
                    setFindings(section, customCSS);
                }
            }

            HashMap<String, List<Object>> map = new HashMap();

            map.put("${summary1}",
                    this.wrapHTML(this.assessment.getSummary() == null ? "" : this.assessment.getSummary(), customCSS,
                            "summary1"));
            map.put("${summary2}",
                    this.wrapHTML(this.assessment.getRiskAnalysis() == null ? "" : this.assessment.getRiskAnalysis(),
                            customCSS, "summary2"));
            replaceHTML(mlp.getMainDocumentPart(), map, false);
            replaceAssessment(customCSS);
            updateDocWithExtensions(customCSS);

            return mlp;
        } finally {
            context.end();
        }
    }

    @ProfileMethod("Convert HTML content to docx format with CSS styling")
    private List<Object> wrapHTML(String content, String customCSS,
            String className) throws Docx4JException {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "wrapHTML");
        try {
            
            if (className == null) {
                className = "";
            }
            if(!content.isEmpty()) {
				content = replacement(content);
				// fix extra spaces
				// content = content.replaceAll("\n", "<br />");
				// content = content.replaceAll("</p><p><br /></p><p>", "<br
				// /></p><p>");//replace extra space
				content = content.replaceAll("<p><br /></p>", "");// replace extra space
				content = content.replaceAll("<blockquote>", "<center class='figure'>");
				content = content.replaceAll("</blockquote>", "</center>");
            }
			LoggingConfig.configureOpenHTMLTopDFLogging();
			String html = "<!DOCTYPE html><html><head>"
					+ "<style>html{padding:0;margin:0;margin-right:0px;}\r\nbody{padding:0;margin:0;font-family:"
					+ this.FONT + ";}\r\n" + customCSS + "</style>" + "</head><body><div class='" + className + "'>"
					+ content + "</div></body></html>";

            String md5 = toMD5(html);
            if (nodeMap.containsKey(md5)) {
                return nodeMap.get(md5);
            } else {
				XHTMLImporterImpl xhtml = new XHTMLImporterImpl(mlp);
				RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
				rfonts.setAscii(this.FONT);
				XHTMLImporterImpl.addFontMapping("Arial", rfonts);
				XHTMLImporterImpl.addFontMapping("arial", rfonts);
                List<Object> converted = xhtml.convert(html, null);
                nodeMap.put(md5, converted);
                return converted;       
            }
        } finally {
            context.end();
        }
    }

    @ProfileMethod("Replace assessment data variables throughout document")
    // Replacement for assessment data
    private void replaceAssessment(String customCSS) throws Exception {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceAssessment");
        try {
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat("MM/dd/yyyy");

            String assessors_nl = "";
            String assessors_comma = "";
            String assessors_bullets = "<ul>";
            boolean isfirst = true;
            for (User hacker : this.assessment.getAssessor()) {
                assessors_nl += hacker.getFname() + " " + hacker.getLname() + "<br/>";
                assessors_comma += (isfirst ? "" : ", ") + hacker.getFname() + " " + hacker.getLname();
                assessors_bullets += "<li class='bullets'>" + hacker.getFname() + " " + hacker.getLname() + "</li>";
                isfirst = false;
            }
            assessors_bullets += "</ul>";
            HashMap<String, String> map = new HashMap<>();
            map.put(getKey("asmtname"), this.assessment.getName());
            map.put(getKey("asmtid"), "" + this.assessment.getId());
            map.put(getKey("asmtappid"), "" + this.assessment.getAppId());
            map.put(getKey("asmtassessor"),
                    "" + this.assessment.getAssessor().get(0).getFname() + " "
                            + this.assessment.getAssessor().get(0).getLname());
            map.put(getKey("asmtassessor_email"), "" + this.assessment.getAssessor().get(0).getEmail());

            map.put(getKey("asmtassessors_comma"), assessors_comma);
            if (this.assessment.getRemediation() == null || this.assessment.getRemediation().getFname() == null
                    || this.assessment.getRemediation().getLname() == null) {
                map.put(getKey("remediation"), "");
            } else {
                map.put(getKey("remediation"), "" + this.assessment.getRemediation().getFname() + " "
                        + this.assessment.getRemediation().getLname());
            }
            map.put(getKey("asmtteam"),
                    this.assessment.getAssessor() == null ? ""
                            : this.assessment.getAssessor().get(0).getTeam() == null ? ""
                                    : this.assessment.getAssessor().get(0).getTeam().getTeamName().trim());
            map.put(getKey("asmttype"), this.assessment.getType().getType());
            // map.put(getKey("today"), formatter.format(new Date()));
            // map.put(getKey("asmtstart"), formatter.format(this.assessment.getStart()));
            // map.put(getKey("asmtend"), formatter.format(this.assessment.getEnd()));
            replacementDate("today", new Date());
            replacementDate("asmtStart", this.assessment.getStart());
            replacementDate("asmtEnd", this.assessment.getEnd());
            map.put(getKey("asmtaccesskey"), this.assessment.getGuid());
            map.put(getKey("totalopenvulns"), this.getTotalOpenVulns(this.assessment.getVulns()));
            map.put(getKey("totalclosedvulns"), this.getTotalClosedVulns(this.assessment.getVulns()));
            map.putAll(getVulnMap());

    		if (this.assessment.getCustomFields() != null) {
    			for (CustomField cf : this.assessment.getCustomFields()) {
    				if(cf.getType().getFieldType() < 3) {
    					map.put("cf" + cf.getType().getVariable(), cf.getValue());
    				}
    			}
    		}
    		replacementHyperlinks(mlp.getMainDocumentPart(),map);
    		replacementText(map);

    		// replavce all cusotm varables
    		Map<String, List<Object>> cfMap = new HashMap<>();
    		if (this.assessment.getCustomFields() != null) {
    			for (CustomField cf : this.assessment.getCustomFields()) {
    				if(cf.getType().getFieldType() == 3) {
    					cfMap.put("${cf" + cf.getType().getVariable() +"}", wrapHTML(cf.getValue(), customCSS, cf.getType().getVariable()));
    				}
    			}
    		} 

            // replace with HTML content
            Map<String, List<Object>> map2 = new HashMap<>();
            map2.put("${asmtAssessors_Lines}", wrapHTML(assessors_nl, customCSS, ""));
            map2.put("${asmtAssessors_Bullets}", wrapHTML(assessors_bullets, customCSS, ""));
            map2.put("${asmtAssessors_Comma}", wrapHTML(assessors_comma, customCSS, ""));
            replaceHTML(mlp.getMainDocumentPart(), map2);
            replaceHTML(mlp.getMainDocumentPart(), cfMap, false);
            replaceHeaderAndFooter(map);
        } finally {
            context.end();
        }

    }

    private String loopReplace(String content) {
        for (int i = 9; i >= 0; i--) {
            content = innerLoop(content, i);
        }
        return content;

    }

    private String innerLoop(String content, int rank) {
        Vulnerability tmp = new Vulnerability();
        String Var = tmp.vulnStr(new Long(rank)).toUpperCase();
        if (content.contains("{[asmt" + Var + "]}")) {
            String html = "<ol>\r\n";
            boolean isSomething = false;
            for (Vulnerability v : this.vulns) {
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


    private String getVulnCount(String content) {
        // C,H,M,L,I,R
        int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        // 9,8,7,6,5,4,3,2,1,0
        int totals = 0;
        if (this.vulns != null) {
            for (Vulnerability v : this.vulns) {
                if (v.getOverall() == null || v.getOverall().intValue() == -1)
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

    private Map<String, String> getVulnMap() {
        Map<String, String> maps = new HashMap<>();
        // C,H,M,L,I,R
        int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        // 9,8,7,6,5,4,3,2,1,0
        int totals = 0;
        if (this.vulns != null) {
            for (Vulnerability v : this.vulns) {
                if (v.getOverall() == null || v.getOverall().intValue() == -1)
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


    private String getKey(String key) {
        for (String word : keywords) {
            word = word.replace("\\", "");
            if (word.toLowerCase().equals(key.toLowerCase()))
                return word;
        }
        return "assessment.nothing";
    }

    // replace simple text in hyperlinks
    private void replacementHyperlinks(Object document, Map<String, String> map) {
        for (String key : map.keySet()) {
            String value = map.get(key);
            this.replaceHyperlink(document, "${" + key + " link}", value);
        }
    }

    // Replace simple text
    private void replacementText(Map<String, String> map)
            throws JAXBException, Docx4JException {
        String xml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), false, false);
        for (String key : map.keySet()) {
        	xml = replaceAll(
        			xml, 
        			key,
        			""+ map.get(key) == null ? "" : map.get(key),
        			true);
        }
        mlp.getMainDocumentPart().setContents((org.docx4j.wml.Document) XmlUtils.unmarshalString(xml));

    }

    private void replacementDate(String key, Date date)
            throws JAXBException, Docx4JException {
        String xml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), false, false);
        xml = replaceDateVariable(xml, key, date);
        mlp.getMainDocumentPart().setContents((org.docx4j.wml.Document) XmlUtils.unmarshalString(xml));

    }

    public static String replaceDateVariable(String text, String key, Date date) {
        // Pattern to match ${key} or ${key whitespace format}
        // Group 1 will capture the format if present, or be null if not present
        String patternStr = "\\$\\{\\s*" + Pattern.quote(key) + "(?:\\s+([^}]+))?\\s*\\}";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String dateFormat = matcher.group(1);

            // Use default format if no format was specified
            if (dateFormat == null || dateFormat.trim().isEmpty()) {
                dateFormat = "MM/dd/yyyy";
            } else {
                dateFormat = dateFormat.trim();
            }

            try {
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                String formattedDate = formatter.format(date);
                matcher.appendReplacement(result, formattedDate);
            } catch (Exception e) {
                // If date format is invalid, leave the original text unchanged
                matcher.appendReplacement(result, matcher.group(0));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String getTotalOpenVulns(List<Vulnerability> vulns) {
        return "" + vulns.stream().filter(v -> v.getClosed() == null).collect(Collectors.toList()).size();
    }

    private String getTotalClosedVulns(List<Vulnerability> vulns) {
        return "" + vulns.stream().filter(v -> v.getClosed() != null).collect(Collectors.toList()).size();
    }

    // replace text/html content
    private String replacement(String content) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replacement");
        try {
			SimpleDateFormat formatter;

			formatter = new SimpleDateFormat("MM/dd/yyyy");

			String assessors_nl = "";
			String assessors_comma = "";
			String assessors_bullets = "<ul>";
			boolean isfirst = true;
			for (User hacker : this.assessment.getAssessor()) {
				assessors_nl += hacker.getFname() + " " + hacker.getLname() + "<br/>";
				assessors_comma += (isfirst ? "" : ", ") + hacker.getFname() + " " + hacker.getLname();
				assessors_bullets += "<li class='bullets'>" + hacker.getFname() + " " + hacker.getLname() + "</li>";
				isfirst = false;
			}
			assessors_bullets += "</ul>";

			content = content.replaceAll("\\$\\{asmtName\\}",
					this.assessment.getName() == null ? "" : this.assessment.getName());
			content = content.replaceAll("\\$\\{asmtId\\}",
					this.assessment.getId() == null ? "" : "" + this.assessment.getId());
			content = content.replaceAll("\\$\\{asmtAppId\\}", "" + this.assessment.getAppId());
			content = content.replaceAll("\\$\\{asmtAssessor\\}", this.assessment.getAssessor() == null ? ""
					: (this.assessment.getAssessor().get(0).getFname() + " "
							+ this.assessment.getAssessor().get(0).getLname()));
			content = content.replaceAll("\\$\\{asmtAssessor_Email\\}",
					this.assessment.getAssessor() == null ? "" : (this.assessment.getAssessor().get(0).getEmail()));
			content = content.replaceAll("\\$\\{asmtAssessors_Lines\\}",
					this.assessment.getAssessor() == null ? "" : assessors_nl);
			content = content.replaceAll("\\$\\{asmtAssessors_Comma\\}",
					this.assessment.getAssessor() == null ? "" : assessors_comma);
			content = content.replaceAll("\\$\\{asmtAssessors_Bullets\\}",
					this.assessment.getAssessor() == null ? "" : assessors_bullets);
			content = content.replaceAll("\\$\\{remediation\\}", this.assessment.getRemediation() == null ? ""
					: (this.assessment.getRemediation().getFname() + " " + this.assessment.getRemediation().getLname()));
			content = getVulnCount(content);

			content = content.replaceAll("\\$\\{asmtTeam\\}",
					this.assessment.getAssessor() == null ? ""
							: this.assessment.getAssessor().get(0).getTeam() == null ? ""
									: this.assessment.getAssessor().get(0).getTeam().getTeamName().trim());
			content = content.replaceAll("\\$\\{asmtType\\}",
					this.assessment.getType() == null ? "" : this.assessment.getType().getType().trim());
			// content = content.replaceAll("\\$\\{today\\}", formatter.format(new Date()));
			content = replaceDateVariable(content, "today", new Date());
			content = content.replaceAll("\\$\\{asmtStandND\\}", formatter.format(this.assessment.getEnd()));
			content = content.replaceAll("\\$\\{asmtAccessKey\\}", this.assessment.getGuid());
			content = content.replaceAll("\\$\\{totalOpenVulns\\}", this.getTotalOpenVulns(this.assessment.getVulns()));
			content = content.replaceAll("\\$\\{totalClosedVulns\\}", this.getTotalClosedVulns(this.assessment.getVulns()));


			// Run extensions
			if (this.reportExtension.isExtended()) {
				content = this.reportExtension.updateReport(this.assessment, content);
			}

			content = loopReplace(content);

			// Fix any html weirdness
			
			content = FSUtils.jtidy(content);
			// Fix images
			content = this.replaceImageLinks(content);
			return content;
        }finally {
        	context.end();
        }

    }

    @ProfileMethod("Efficiently replace image links with base64 or remove if not found")
    private String replaceImageLinks(String text) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceImageLinks");
        try {
            if (text.equals("")) {
                return text;
            }
            if(!text.contains("getImage")) {
            	return text;
            }
            text = this.centerImages(text);
            Long aid = this.assessment.getId();

            // Remove undefined image links first
            String badImage = "<img src=\"getImage\\?id(=|&#61;)undefined\" >";
            text = text.replaceAll(badImage, "");

            // Pattern to match image links with both regular and HTML-encoded formats
            // Captures: assessment_id and image_guid
            Pattern imagePattern = Pattern.compile(
                    "<img[^>]+src=\"getImage\\?id(=|&#61;)" + aid + ":([^\"\\s>]+)\"[^>]*>",
                    Pattern.CASE_INSENSITIVE);

            // First pass: find all referenced GUIDs
            Set<String> referencedGuids = new HashSet<>();
            Matcher matcher = imagePattern.matcher(text);
            while (matcher.find()) {
                String guid = matcher.group(2); // The GUID part after the colon
                referencedGuids.add(guid);
            }

            // If no image references found, return text as-is
            if (referencedGuids.isEmpty()) {
                return text;
            }

            // Query database only for referenced GUIDs
            Map<String, String> guidToBase64Map = new HashMap<>();
            EntityManager em = HibHelper.getInstance().getEM();
            try {
                for (String guid : referencedGuids) {
                    try {
                        Image img = (Image) em.createQuery("SELECT i FROM Image i WHERE i.guid = :guid")
                                .setParameter("guid", guid)
                                .getSingleResult();
                        if (img != null && img.getBase64Image() != null) {
                            guidToBase64Map.put(guid, img.getBase64Image());
                        }
                    } catch (Exception e) {
                        // Image not found for this GUID - will be removed from text
                    }
                }
            } finally {
                em.close();
            }

            // Second pass: replace or remove image links
            StringBuffer result = new StringBuffer();
            matcher = imagePattern.matcher(text);
            while (matcher.find()) {
                String guid = matcher.group(2);
                String base64Image = guidToBase64Map.get(guid);

                if (base64Image != null) {
                    // Replace with complete img tag containing base64 data
                    String newImgTag = "<img src=\"" + base64Image + "\" >"; //image tag with </img>
                    matcher.appendReplacement(result, Matcher.quoteReplacement(newImgTag));
                } else {
                    // Remove the image link entirely
                    matcher.appendReplacement(result, "");
                }
            }
            matcher.appendTail(result);

            return result.toString();
        } finally {
            context.end();
        }
    }

    private void outlineImages() {
        for (Image img : this.assessment.getImages()) {
            try {
                String[] parts = img.getBase64Image().split(",");
                String file_dataContentType = parts[0].split(";")[0].replace("data:", "");
                byte[] imageData = Base64.getDecoder().decode(parts[1]);
                imageData = ImageBorderUtil.addBorder(imageData, 1, Color.GRAY);
                String borderedImage = Base64.getEncoder().encodeToString(imageData);
                borderedImage = "data:" + file_dataContentType + ";base64," + borderedImage;
                img.setBase64Image(borderedImage);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @ProfileMethod("Process findings section and populate with vulnerability data")
    private void setFindings(String section, String customCSS)
            throws JAXBException, Docx4JException {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "setFindings");
        try {
            int begin = -1;
            int end = -1;

            if (Strings.isNullOrEmpty(section)) {
                begin = getIndex(mlp.getMainDocumentPart(), "${fiBegin}");
                end = getIndex(mlp.getMainDocumentPart(), "${fiEnd}");
            } else if (section != null && section.equals("Default")) {
                begin = getIndex(mlp.getMainDocumentPart(), "${fiBegin}");
                end = getIndex(mlp.getMainDocumentPart(), "${fiEnd}");
            } else if (section != null) {
                begin = getIndex(mlp.getMainDocumentPart(), "${fiBegin " + section + "}");
                end = getIndex(mlp.getMainDocumentPart(), "${fiEnd " + section + "}");
            }

            if (begin == -1)
                return;
            if (end == -1)
                return;

            HashMap<String, String> colorMap = new HashMap<>();
            HashMap<String, String> cellMap = new HashMap<>();
            HashMap<String, String> customFieldMap = new HashMap<>();
            String noIssuesText = "";

            // Get relevent parts of the document and put them into a
            // temporary array.
            List<Object> findingTemplate = new LinkedList();
            for (int i = begin; i <= end; i++) {
                findingTemplate.add(mlp.getMainDocumentPart().getContent().get(i));
                List<Object> paragraphs = getAllElementFromObject(mlp.getMainDocumentPart().getContent().get(i),
                        P.class);
                String colors = getMatchingText(paragraphs, "${color");
                if (colors != null) {
                    colors = colors.replace("${color", "").replace("}", "").trim();
                    String[] pairs = colors.split(",");

                    for (String pair : pairs) {
                        pair = pair.trim();
                        colorMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

                    }
                }
                colors = getMatchingText(paragraphs, "${fill");
                if (colors != null) {
                    colors = colors.replace("${fill", "").replace("}", "").trim();
                    String[] pairs = colors.split(",");

                    for (String pair : pairs) {
                        pair = pair.trim();
                        cellMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

                    }
                }
                String customFields = getMatchingText(paragraphs, "${custom-fields");
                if (customFields != null) {
                    customFields = customFields.replace("${custom-fields", "").replace("}", "").trim();
                    String[] pairs = customFields.split(",");

                    for (String pair : pairs) {
                        pair = pair.trim();
                        customFieldMap.put(pair.split("=")[0], pair.split("=")[1].toUpperCase());

                    }
                }
                noIssuesText = getMatchingText(paragraphs, "${noIssuesText");
                if (noIssuesText != null) {
                    noIssuesText = noIssuesText.replace("${noIssuesText ", "").replace("}", "");
                } else {
                    noIssuesText = "No issues detected for this section.";
                }
            }
            // Remove the elements from the doc. These will be replaced with
            // out temp array when its updated later on
            for (int i = end; i >= begin; i--) {
                mlp.getMainDocumentPart().getContent().remove(i);
            }
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("MM/dd/yyyy");

            List<Vulnerability> filteredVulns = this.getFilteredVulns(section);

            // No issues found in this section
            if (filteredVulns == null || filteredVulns.size() == 0) {
                ObjectFactory factory = Context.getWmlObjectFactory();
                P p = factory.createP();
                R r = factory.createR();
                Text text = factory.createText();
                text.setValue(noIssuesText);
                r.getContent().add(text);
                p.getContent().add(r);
                mlp.getMainDocumentPart().getContent().add(begin++, p);
                return;
            }
            int index = 0;
            String prevSev = "";
            for (Vulnerability v : filteredVulns) {
                if (prevSev == v.getOverallStr()) {
                    index++;
                } else {
                    prevSev = v.getOverallStr();
                    index = 1;
                }

                for (Object obj : findingTemplate) {

                    String xml = XmlUtils.marshaltoString(obj, false, false);
                    if (xml == "") {
                        continue;
                    }
                    String nxml = replaceXml(xml,v,customFieldMap,colorMap,cellMap,cellMap,index,0);
                    // remove color loops
                    if (nxml.contains("${color") || nxml.contains("${fill") || nxml.contains("${custom-fields"))
                    	nxml = "";
                    
                    if (nxml != "") {
                        try {
                            Object paragraph = XmlUtils.unmarshalString(nxml);
                            // Replace Hyperlinks
                            for (CustomField cf : v.getCustomFields()) {
                                this.replaceHyperlink(paragraph, "${cf" + cf.getType().getVariable() + " link}",
                                        cf.getValue());
                            }
                            this.replaceHyperlink(paragraph, "${cvssString link}", v.getCvssString());
                            mlp.getMainDocumentPart().getContent().add(begin++, paragraph);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println(nxml);
                        }
                    }

                }
            }

            // Add the vulnerability description and recommandations.
            // update custom fields inside the html before inserting into the document.
            int count = 1;
            for (Vulnerability v : filteredVulns) {
				HashMap<String, List<Object>> detailsMap = new HashMap<>();
				
				String rec = getRecommendation(v);
				rec = this.replaceFigureVariables(rec, count);
				detailsMap.put("${rec}", wrapHTML(rec, customCSS, "rec"));
				
				String desc = getDescription(v);
				desc = this.replaceFigureVariables(desc, count);
				detailsMap.put("${desc}", wrapHTML(desc, customCSS, "desc"));
				
				String details = getDetails(v);
				details = this.replaceFigureVariables(details, count);
				detailsMap.put("${details}", wrapHTML(details, customCSS, "details"));
				

                if (v.getCustomFields() != null) {
                    for (CustomField cf : v.getCustomFields()) {
                        if (cf.getType().getFieldType() == 3) {
                            detailsMap.put("${cf" + cf.getType().getVariable() + "}",
                                    wrapHTML(cf.getValue(), customCSS, cf.getType().getVariable()));
                        }
                    }
                }

                replaceHTML(mlp.getMainDocumentPart(), detailsMap, true);
                count++;

            }
        } finally {
            context.end();
        }

    }

    @ProfileMethod("Get all Elements by Type")
    private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getAllElementsFromObject");
        try {
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
        }finally {
        	context.end();
        }
    }


    /*
     * Utility function to find elements in the docx file
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

    @ProfileMethod("Index of cell")
    private int indexOfCell(Tbl table, List<Object> paragraphs, String variable) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "indexOfCell");
        try {
			for (Object para : paragraphs) {
				if (matchText((P) para, variable)) {

					Tc cell = ((Tc) ((P) para).getParent());
					if (cell.getParent().getClass().getName().equals("org.docx4j.wml.Tr")) {
						return ((Tr) cell.getParent()).getContent().indexOf(cell);
					}
				}
			}
			return -1;
        }finally {
        	context.end();
        }
    }

    /*
     * Check if there is a match in the text
     */
    private boolean matchText(P paragraph, String variable) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "matchText");
        try {
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
        }finally{
        	context.end();
        }
    }

    private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements) {
        replaceHTML(mainPart, replacements, true);
    }

    @ProfileMethod("Get Paragraphs")
    private List<P> getParagraphs(final Object mainPart) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getParagraphs");
        try {
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
			return paragraphs;
        }finally {
        	context.end();
        }

    }

    private List<P.Hyperlink> getHyperLinks(final Object mainPart) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getHyperLinks");
        try {
			final List<P.Hyperlink> links = new ArrayList<>();
			new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
				@Override
				public List<Object> apply(Object o) {
					if (o instanceof P.Hyperlink) {
						links.add((P.Hyperlink) o);
					}
					return null; // Continue traversal
				}

				@Override
				public boolean shouldTraverse(Object o) {
					return true; // Traverse all elements
				}
			});
			return links;
        }finally {
        	context.end();
        }
    }

    @ProfileMethod("Replace hyperlink text and URL in document")
    public void replaceHyperlink(Object wordPackage, String searchText, String newUrl) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHyperlink");
        try {
            // Find all hyperlinks
            List<P.Hyperlink> hyperlinks = getHyperLinks(wordPackage);

            for (P.Hyperlink hyperlink : hyperlinks) {
                // Check if this hyperlink contains the text we're looking for
                String hyperlinkText = getHyperlinkDisplayText(hyperlink);
                if (hyperlinkText != null && hyperlinkText.contains(searchText)) {
                    // Update the variable in the hyperlink text
                    String updatedHyperlink = hyperlinkText.replace(searchText, newUrl);

                    // Create new relationship (don't worry about removing old one)
                    RelationshipsPart relsPart = this.mlp.getMainDocumentPart().getRelationshipsPart();

                    // Add new hyperlink relationship
                    org.docx4j.relationships.Relationship newRel = new org.docx4j.relationships.Relationship();
                    newRel.setId(relsPart.getNextId()); // Get next available ID
                    newRel.setType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
                    String updatedTarget = updatedHyperlink;
                    if (updatedTarget.contains("@")) {
                        updatedTarget = "mailto:" + updatedTarget;
                    } else if (!updatedTarget.startsWith("http")) {
                        updatedTarget = "https://" + updatedTarget;
                    }
                    if (searchText.contains("cvssString link")) {
                        if (this.assessment.getType().isCvss31()) {
                            updatedTarget = "https://www.first.org/cvss/calculator/3-1#" + newUrl;
                        } else {
                            updatedTarget = "https://www.first.org/cvss/calculator/4-0#" + newUrl;
                        }

                    }
                    newRel.setTarget(updatedTarget);
                    newRel.setTargetMode("External");

                    // Add the relationship
                    relsPart.getRelationships().getRelationship().add(newRel);

                    // Update the hyperlink element with new relationship ID
                    hyperlink.setId(newRel.getId());

                    // Update the display text
                    updateHyperlinkDisplayText(hyperlink, updatedHyperlink);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context.end();
        }
    }

    private String getHyperlinkDisplayText(P.Hyperlink hyperlink) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getHyperlinkDisplayText");
        try {
			StringBuilder text = new StringBuilder();
			for (Object obj : hyperlink.getContent()) {
				if (obj instanceof R) {
					R run = (R) obj;
					for (Object runContent : run.getContent()) {
						if (runContent instanceof Text) {
							text.append(((Text) runContent).getValue());
						} else if (runContent instanceof JAXBElement) {
							JAXBElement<?> element = (JAXBElement<?>) runContent;
							if (element.getValue() instanceof Text) {
								text.append(((Text) element.getValue()).getValue());
							}
						}
					}
				}
			}
			return text.toString();
        }finally {
        	context.end();
        }
    }

    private void updateHyperlinkDisplayText(P.Hyperlink hyperlink, String newText) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "updateHyperlinkDisplayText");
        try {
			// Clear existing content
			hyperlink.getContent().clear();

			// Create new run with the text
			R run = new R();

			// Add hyperlink style
			RPr runProps = new RPr();
			RStyle hyperlinkStyle = new RStyle();
			hyperlinkStyle.setVal("Hyperlink");
			runProps.setRStyle(hyperlinkStyle);
			run.setRPr(runProps);

			// Add the text
			Text text = new Text();
			text.setValue(newText);
			run.getContent().add(text);

			hyperlink.getContent().add(run);
        }finally {
        	context.end();
        }
    }

    // This function is needed to get better processing around images
    // that can used to center them
    private String centerImages(String content) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "centerImages");
        try {

			int index = content.indexOf("<img ");
			while (index != -1) {
				String first = content.substring(0, index);
				String second = content.substring(index, content.length());
				second = second.replaceFirst("/>", "></img>");
				content = first + second;
				index = content.indexOf("<img ", index + 1);

			}
			content = content.replaceAll("<p><img", "<center><img");
			content = content.replaceAll("</img><br /></p>", "</img></center>");
			return content;
        }finally {
        	context.end();
        }
    }

    private void replaceHeaderAndFooter(final Map<String, String> replacements) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHeaderAndFooter");
        try {
			List<Object> list = new ArrayList<>();
			try {

				if (this.mlp.getHeaderFooterPolicy().getDefaultHeader() != null) {
					HeaderPart fp = this.mlp.getHeaderFooterPolicy().getDefaultHeader();
					String xml = XmlUtils.marshaltoString(fp.getContents(), false, true);
					for (String key : replacements.keySet()) {
						xml = xml.replace("${" + key + "}", replacements.get(key));
					}
					this.mlp.getHeaderFooterPolicy().getDefaultHeader().setContents((Hdr) XmlUtils.unmarshalString(xml));
				}
				if (this.mlp.getHeaderFooterPolicy().getFirstHeader() != null) {
					HeaderPart fp = this.mlp.getHeaderFooterPolicy().getFirstHeader();
					String xml = XmlUtils.marshaltoString(fp.getContents(), false, true);
					for (String key : replacements.keySet()) {
						xml = xml.replace("${" + key + "}", replacements.get(key));
					}
					this.mlp.getHeaderFooterPolicy().getFirstHeader().setContents((Hdr) XmlUtils.unmarshalString(xml));
				}
				if (this.mlp.getHeaderFooterPolicy().getEvenHeader() != null) {
					HeaderPart fp = this.mlp.getHeaderFooterPolicy().getEvenHeader();
					String xml = XmlUtils.marshaltoString(fp.getContents(), false, true);
					for (String key : replacements.keySet()) {
						xml = xml.replace("${" + key + "}", replacements.get(key));
					}
					this.mlp.getHeaderFooterPolicy().getEvenHeader().setContents((Hdr) XmlUtils.unmarshalString(xml));

				}
				if (this.mlp.getHeaderFooterPolicy().getDefaultFooter() != null) {
					FooterPart fp = this.mlp.getHeaderFooterPolicy().getDefaultFooter();
					String xml = XmlUtils.marshaltoString(fp.getContents(), false, true);
					for (String key : replacements.keySet()) {
						xml = xml.replace("${" + key + "}", replacements.get(key));
					}
					this.mlp.getHeaderFooterPolicy().getDefaultFooter().setContents((Ftr) XmlUtils.unmarshalString(xml));
				}
				if (this.mlp.getHeaderFooterPolicy().getFirstFooter() != null) {
					FooterPart fp = this.mlp.getHeaderFooterPolicy().getFirstFooter();
					String xml = XmlUtils.marshaltoString(fp.getContents(), false, true);
					for (String key : replacements.keySet()) {
						xml = xml.replace("${" + key + "}", replacements.get(key));
					}
					this.mlp.getHeaderFooterPolicy().getFirstFooter().setContents((Ftr) XmlUtils.unmarshalString(xml));
				}
				if (this.mlp.getHeaderFooterPolicy().getEvenFooter() != null) {
					FooterPart fp = this.mlp.getHeaderFooterPolicy().getEvenFooter();
					String xml = XmlUtils.marshaltoString(fp.getContents(), false, true);
					for (String key : replacements.keySet()) {
						xml = xml.replace("${" + key + "}", replacements.get(key));
					}
					this.mlp.getHeaderFooterPolicy().getEvenFooter().setContents((Ftr) XmlUtils.unmarshalString(xml));

				}

			} catch (XPathBinderAssociationIsPartialException | JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }finally {
        	context.end();
        }
    }

    @ProfileMethod("replace HTML")
    private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements, boolean once) {
    	if(replacements.size() == 0)
    		return;
    	
        if (mainPart == null)
            return;
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHTML");
        try {
			Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
			Preconditions.checkNotNull(replacements, "replacements may not be null!");

			List<P> paragraphs = this.getParagraphs(mainPart);
			

			// run through all found paragraphs to located identifiers
			for (final P paragraph : paragraphs) {
				
				// convert paragraph to raw text
				final StringWriter paragraphText = new StringWriter();
				try {
					TextUtils.extractText(paragraph, paragraphText);
				} catch (Exception ex) {

				}
				final String identifier = paragraphText.toString().trim();
				// if raw text is our variable then replace the paragraph
				// with formated text
				if (identifier != null && identifier.indexOf("${") != -1 && replacements.containsKey(identifier)) {
					
					final List<Object> listToModify = this.getParentElement(paragraph);

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
        }finally {
        	context.end();
        }
    }

    @ProfileMethod("Replace figure variables in text with numbered format")
    public String replaceFigureVariables(String text, int index) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceFigureVariables");
        try {
            // Pattern to match ${Figure#.X} where X is one or more digits
            Pattern pattern = Pattern.compile("\\$\\{Figure#\\.(\\d+)\\}");
            Matcher matcher = pattern.matcher(text);

            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String subNumber = matcher.group(1);
                String replacement = "Figure " + index + "." + subNumber;
                matcher.appendReplacement(result, replacement);
            }

            matcher.appendTail(result);
            return result.toString();
        } finally {
            context.end();
        }
    }

    private List<Object> getParentElement(P paragraph) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getParentElement");
        try {
			if (paragraph.getParent() instanceof Tc) {
				// paragraph located in table-cell
				final Tc parent = (Tc) paragraph.getParent();
				return parent.getContent();
			} else if (paragraph.getParent() instanceof Hdr) {
				// located in a header element
				final Hdr parent = (Hdr) paragraph.getParent();
				return parent.getContent();
			} else if (paragraph.getParent() instanceof CTTxbxContent) {
				final CTTxbxContent parent = (CTTxbxContent) paragraph.getParent();
				return parent.getContent();
			} else {
				// paragraph located in main document part
				return ((MainDocumentPart) mlp.getMainDocumentPart()).getContent();
			}
        }finally {
        	context.end();
        }

    }

    private void updateDocWithExtensions(String customCSS) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "updateDocWithExtensions");
        try {

			MainDocumentPart mainPart = mlp.getMainDocumentPart();
			// look for all P elements in the specified object
			List<P> paragraphs = this.getParagraphs(mainPart);

			// run through all found paragraphs to located identifiers
			for (final P paragraph : paragraphs) {
				// convert paragraph to raw text
				final StringWriter paragraphText = new StringWriter();
				try {
					TextUtils.extractText(paragraph, paragraphText);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				final String identifier = paragraphText.toString().trim();
				final List<Object> listToModify = this.getParentElement(paragraph);

				if (listToModify != null) {
					final int index = listToModify.indexOf(paragraph);
					Preconditions.checkState(index > -1, "could not located the paragraph in the specified list!");
					if (this.reportExtension.isExtended()) {
						String html = this.reportExtension.updateReport(this.assessment, identifier);
						if (html != null && !html.equals(identifier)) {
							listToModify.remove(index);
							try {
								listToModify.addAll(index, this.wrapHTML(html, customCSS, ""));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

					}

				}

			}
        }finally {
        	context.end();
        }

    }

    @ProfileMethod("Generate table of contents in document")
    public void tocGenerator(WordprocessingMLPackage mlp) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "tocGenerator");
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
        } finally {
            context.end();
        }

    }

    @ProfileMethod("Addes a Page Break")
    private void addPageBreak(WordprocessingMLPackage mlp, int index) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "addpageBreak");
        try {
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
        }finally {
        	context.end();
        }

    }

    @ProfileMethod("Gets an index based on keyword")
    private int getIndex(final MainDocumentPart mainPart, String keyword) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getIndex");
        try {
			Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");

			// look for all P elements in the specified object
			final List<P> paragraphs = this.getParagraphs(mainPart);

			// run through all found paragraphs to located identifiers
			for (final P paragraph : paragraphs) {
				// check if this is one of our identifiers
				final StringWriter paragraphText = new StringWriter();
				try {
					TextUtils.extractText(paragraph, paragraphText);
				} catch (Exception ex) {

				}

				final String identifier = paragraphText.toString();
				if (identifier != null &&  identifier.contains("${") && identifier.contains(keyword)) {
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
        }finally {
        	context.end();
        }
    }

    @ProfileMethod("Gets Matching paragraphs from text")
    private String getMatchingText(P paragraph, String variable) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getMathingText");
        final StringWriter paragraphText = new StringWriter();
        try {
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
        }finally {
        	context.end();
        }
    }

    @ProfileMethod("Gets Matching paragraphs from text")
    private String getMatchingText(List<Object> paragraphs, String variable) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getMathingText");
        try {
			for (Object paragraph : paragraphs) {
				String text = getMatchingText((P) paragraph, variable);
				if (text != null)
					return text;
			}
			return null;
        }finally {
        	context.end();
        }
    }

    private static String toMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 is guaranteed to exist in Java, so this should never happen
            throw new RuntimeException(e);
        }
    }
    private static String replaceAll(String original, String pattern, String replacement, Boolean wrapCDATA) {
    	if(wrapCDATA) {
    		return StringUtils.replace(original, "${" + pattern + "}", CData(replacement));
    	}else {
    		return StringUtils.replace(original, "${" + pattern + "}", replacement);
    	}
    }
    
    private static String replaceAllCf(String original,Vulnerability v) {
		if (v.getCustomFields() != null) {
			for (CustomField cf : v.getCustomFields()) {
				try {
					original = replaceAll(original, "cf" + cf.getType().getVariable(), cf.getValue(), true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return original;
    }
    private static String getRecommendation(Vulnerability v) {
    	String rec = "";
		if (v.getRecommendation() == null && v.getDefaultVuln() != null) {
			rec = v.getDefaultVuln().getRecommendation();
		} else if (v.getRecommendation() != null) {
			rec = v.getRecommendation();
		}
		if(!rec.isEmpty()) {
			return replaceAllCf(rec, v);
		}else {
			return rec;
		}
    }
    private static String getDescription(Vulnerability v) {
    	String desc = "";
		if (v.getDescription() == null && v.getDefaultVuln() != null) {
			desc = v.getDefaultVuln().getDescription();
		} else if (v.getDescription() != null) {
			desc = v.getDescription();
		}
		if(!desc.isEmpty()) {
			return replaceAllCf(desc, v);
		}else {
			return desc;
		}
    }
    private static String getDetails(Vulnerability v) {
    	String details = ""; 
		if (v.getDetails() != null) {
			return v.getDetails();
		}
		if(!details.isEmpty()) {
			return replaceAllCf(details, v);
		}else {
			return details;
		}
    }
    private static String replaceXml(String xml, 
    		Vulnerability v, 
    		HashMap<String,String> customFieldMap, 
    		HashMap<String,String> colorMap, 
    		HashMap<String,String> cellMap, 
    		HashMap<String,String> fillMap, 
    		int count, 
    		int sevIndex) {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("MM/dd/yyyy");
		String nxml = replaceAll(xml, "vulnName", v.getName(), true);
		nxml = replaceAll(nxml, "severity", v.getOverallStr(), true);
		nxml = replaceAll(nxml, "impact", v.getImpactStr(), true);
		nxml = replaceAll(nxml, "likelihood", v.getLikelyhoodStr(), true);
		nxml = replaceAll(nxml, "cvssScore", v.getCvssScore(), true);
		nxml = replaceAll(nxml, "cvssString", v.getCvssString(), true);
		nxml = replaceAll(nxml, "tracking", v.getTracking(), true);
		if (v.getOpened() != null) {
			nxml = replaceAll(nxml, "openedAt", formatter.format(v.getOpened()), false);
		} else {
			nxml = replaceAll(nxml, "openedAt", "", false);
		}
		if (v.getClosed() != null) {
			nxml = replaceAll(nxml, "closedAt", formatter.format(v.getClosed()), false);
		} else {
			nxml = replaceAll(nxml, "closedAt", "", false);
		}
		if (v.getDevClosed() != null) {
			nxml = replaceAll(nxml, "closedInDevAt", formatter.format(v.getDevClosed()), false);
		} else {
			nxml = replaceAll(nxml, "closedInDevAt", "", false);
		}
		try {
			nxml = replaceAll(nxml, "vid", "" + v.getId(), false);
		} catch (Exception ex) {
		}
		nxml = replaceAll(nxml, 
				"category",
				v.getCategory() == null ? "UnCategorized" : v.getCategory().getName(), 
				true);
		
		if (v.getClosed() == null)
			nxml = replaceAll(nxml, "remediationStatus", "Open", false);
		else
			nxml = replaceAll(nxml, "remediationStatus", "Closed", false);

		nxml = replaceAll(nxml, "count", "" + count, false);
		nxml = replaceAll(nxml, "loop", "" , false);
		nxml = nxml.replaceAll("\\$\\{loop\\-[0-9]+\\}", "");

		if (v.getOverallStr() != null && !v.getOverallStr().equals("")) {
			nxml = replaceAll(nxml, "sevId", "" + v.getOverallStr().charAt(0) + "V" + sevIndex, false);
		} else {
			nxml = replaceAll(nxml, "sevId", "V" + sevIndex, false);
		}

		if (v.getCustomFields() != null) {
			for (CustomField cf : v.getCustomFields()) {
				// Only perform this action if the variable is plain text and not a hyperlink
				/*
				 * Boolean isHyperlink = nxml.matches(
				 * ".*<w:hyperlink w:history=\"true\" r:id=\".*\"><w:r><w:rPr><w:rStyle w:val=\"Hyperlink\"/></w:rPr><w:t>\\$\\{cf"
				 * +cf.getType().getVariable()+"\\}.*");
				 * if(cf.getType().getFieldType() < 3 && !isHyperlink) {
				 */

				if (cf.getType().getFieldType() < 3) {
					nxml = StringUtils.replace(nxml, "${cf" + cf.getType().getVariable() + "}", CData(cf.getValue()));
					if (customFieldMap.containsKey(cf.getType().getVariable())
							&& colorMap.containsKey(cf.getValue())) {
						String colorMatch = customFieldMap.get(cf.getType().getVariable());
						String color = colorMap.get(cf.getValue());
						if (colorMatch != null && colorMatch != "" && color != null && color != "") {
							// Change Custom Field Font Colors
							nxml = StringUtils.replace(nxml, "w:val=\"" + colorMatch + "\"", "w:val=\"" + color + "\"");
						}
					}
					if (customFieldMap.containsKey(cf.getType().getVariable())
							&& cellMap.containsKey(cf.getValue())) {
						String colorMatch = customFieldMap.get(cf.getType().getVariable());
						String color = cellMap.get(cf.getValue());
						if (colorMatch != null && colorMatch != "" && color != null && color != "") {
							// Change Custom Field background Colors
							nxml = StringUtils.replace(nxml, "w:fill=\"" + colorMatch + "\"", "w:fill=\"" + color + "\"");
						}
					}
				}
			}
		}

		// change border colors:
		nxml = StringUtils.replace(nxml, "w:color=\"FAC701\"", "w:color=\"" + colorMap.get(v.getOverallStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:color=\"FAC701\"", "w:color=\"" + colorMap.get(v.getOverallStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:color=\"FAC702\"", "w:color=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:color=\"FAC703\"",
				"w:color=\"" + colorMap.get(v.getImpactStr()) + "\"");

		// Fill Cells
		nxml = StringUtils.replace(nxml, "w:fill=\"FAC701\"",
				"w:fill=\"" + cellMap.get(v.getOverallStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:fill=\"FAC702\"",
				"w:fill=\"" + cellMap.get(v.getLikelyhoodStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:fill=\"FAC703\"", "w:fill=\"" + cellMap.get(v.getImpactStr()) + "\"");

		// Change font colors:
		nxml = StringUtils.replace(nxml, "w:val=\"FAC701\"", "w:val=\"" + colorMap.get(v.getOverallStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:val=\"FAC702\"",
				"w:val=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
		nxml = StringUtils.replace(nxml, "w:val=\"FAC703\"", "w:val=\"" + colorMap.get(v.getImpactStr()) + "\"");
		
		return nxml;
    	
    }

}