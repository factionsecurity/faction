package com.fuse.reporting;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
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
import org.docx4j.wml.Numbering;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase.Ind;
import org.dom4j.CDATA;
import org.xml.sax.SAXParseException;
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
import com.fuse.utils.ReportImageScaler;
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
    // per-report caches: image lookups and the template's table list are
    // reused across every vulnerability field instead of being rebuilt.
    // The image cache is LRU-bounded so image-heavy assessments cannot
    // hold every base64 payload in memory at once. Images are downscaled
    // before caching, so the cap comfortably covers hundreds of images.
    private static final long MAX_IMAGE_CACHE_CHARS = 64L * 1024 * 1024; // ~128MB of cached base64
    private LinkedHashMap<String, String> imageCache = new LinkedHashMap<>(16, 0.75f, true);
    private long imageCacheChars = 0;
    private HashSet<String> imagesNotFound = new HashSet<>();
    // oversized images are shrunk to this width before conversion — see
    // ReportImageScaler; 0 disables
    private final int maxImageWidth = ReportImageScaler.configuredMaxWidth();
    // downscaled images prepared by warmImageCache for THIS report's fields.
    // Deliberately not size-capped: it holds at most the report's own
    // (downscaled) images — which the document will embed anyway — and a cap
    // here caused LRU thrashing where warmed entries were evicted and then
    // re-fetched and re-downscaled serially, one field at a time
    private HashMap<String, String> warmedImages = new HashMap<>();
    // inline data-URI images downscaled ahead of time (in parallel) by
    // warmImageCache, keyed by hash of the original URI
    private ConcurrentHashMap<String, String> inlineImagesDownscaled = new ConcurrentHashMap<>();
    // matches <img src="getImage?id=<assessment>:<guid>"> with either quote
    // style and HTML-encoded '='
    private static final Pattern IMAGE_LINK_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']getImage\\?id(=|&#61;)[0-9]+:([^\"'\\s>]+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE);
    private List<Object> templateTables;
    private Set<Object> processedTables = java.util.Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
    // assessment-level values that never change during a report run
    private String assessorsLines = "";
    private String assessorsComma = "";
    private String assessorsBullets = "";
    private int[] riskCounts = new int[10];
    private int riskTotal = 0;
    private String totalOpenVulns = "0";
    private String totalClosedVulns = "0";
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd/yyyy");
        }
    };

    public DocxUtils(EntityManagerFactory entityManagerFactory, WordprocessingMLPackage mlp, Assessment assessment) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "DocxUtils");
        try {
            this.mlp = mlp;
            this.reportExtension = new Extensions(entityManagerFactory, Extensions.EventType.REPORT_MANAGER);
            this.assessment = assessment;
            //this.outlineImages();
            this.vulns = assessment.getVulns();
            this.precomputeAssessmentValues();
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
            this.precomputeAssessmentValues();
            this.setupReportSections(HibHelper.getInstance().getEMF());
        } finally {
            context.end();
        }
    }

    // computed once per report instead of once per content field
    private void precomputeAssessmentValues() {
        if (this.assessment.getAssessor() != null) {
            StringBuilder nl = new StringBuilder();
            StringBuilder comma = new StringBuilder();
            StringBuilder bullets = new StringBuilder("<ul>");
            boolean isfirst = true;
            for (User hacker : this.assessment.getAssessor()) {
                nl.append(hacker.getFname()).append(" ").append(hacker.getLname()).append("<br/>");
                comma.append(isfirst ? "" : ", ").append(hacker.getFname()).append(" ").append(hacker.getLname());
                bullets.append("<li class='bullets'>").append(hacker.getFname()).append(" ").append(hacker.getLname())
                        .append("</li>");
                isfirst = false;
            }
            bullets.append("</ul>");
            this.assessorsLines = nl.toString();
            this.assessorsComma = comma.toString();
            this.assessorsBullets = bullets.toString();
        }
        if (this.vulns != null) {
            int open = 0;
            int closed = 0;
            for (Vulnerability v : this.vulns) {
                if (v.getClosed() == null) {
                    open++;
                } else {
                    closed++;
                }
                if (v.getOverall() == null || v.getOverall().intValue() == -1)
                    continue;
                riskCounts[v.getOverall().intValue()]++;
                riskTotal++;
            }
            this.totalOpenVulns = "" + open;
            this.totalClosedVulns = "" + closed;
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
			Boolean hasVulns = !filteredVulns.isEmpty();
			this.removeSectionTagsOrRemoveSection(section, hasVulns);
			
			if(!hasVulns) {
				return;
			}

            // the template's tables are collected once per report — later
            // passes would otherwise re-traverse a document that has grown
            // by hundreds of generated rows
            if (this.templateTables == null) {
                this.templateTables = getAllElementFromObject(mlp.getMainDocumentPart(), Tbl.class);
            }
            List<Object> tables = this.templateTables;
            for (Object table : tables) {
                // a table consumed by an earlier section had its template rows
                // deleted and only grows with generated rows — skip rescans
                if (this.processedTables.contains(table)) {
                    continue;
                }
            	MethodProfiler.ProfileContext context2 = MethodProfiler.start("DocxUtils", "sectionTest");
                List<Object> paragraphs = getAllElementFromObject(table, P.class);
                String tableVariable = "${" + variable + "}";
                if (ReportFeatures.allowSections() && section != null && !section.isEmpty()
                        && !section.equals("Default")) {
                    tableVariable = "${" + variable + " " + section + "}";
                }
                String txt = getMatchingText(paragraphs, tableVariable);
                if (txt == null)
                    continue;

                this.processedTables.add(table);

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
                // HTML placeholders get a per-vulnerability suffix
                // (e.g. ${rec:3}) so every field of the table can be
                // converted in one batched pass instead of one importer
                // call per field — the importer's fixed cost (CSS parse,
                // font metrics, layout setup) dominated this path.
                // Pre-compiled cache hits skip conversion entirely.
                List<String[]> fieldSpecs = new ArrayList<>();
                Set<String> collectedKeys = new HashSet<>();
                HashMap<String, List<Object>> pendingNodes = new HashMap<>();
                List<Object[]> pendingRows = new ArrayList<>(); // {Tr row, List<String> row keys}
                for (Vulnerability v : filteredVulns) {
                    if (prevSev == v.getOverallStr()) {
                        sevIndex++;
                    } else {
                        prevSev = v.getOverallStr();
                        sevIndex = 1;
                    }
                    for (String xml : xmls) {
                    	String nxml = replaceXml(xml, v, customFieldMap, colorMap, cellMap, null, count, sevIndex);
                        List<String> rowKeys = new ArrayList<>();

                        if (xml.contains("${rec}")) {
                            String key = "${rec:" + count + "}";
                            nxml = StringUtils.replace(nxml, "${rec}", key);
                            rowKeys.add(key);
                            if (collectedKeys.add(key)) {
                                List<Object> cached = wrapHTMLFromCache(v, "rec", count, customCSS);
                                if (cached != null) {
                                    pendingNodes.put(key, cached);
                                } else {
                                    fieldSpecs.add(new String[] { key, "rec",
                                            this.replaceFigureVariables(getRecommendation(v), count) });
                                }
                            }
                        }
                        if (xml.contains("${desc}")) {
                            String key = "${desc:" + count + "}";
                            nxml = StringUtils.replace(nxml, "${desc}", key);
                            rowKeys.add(key);
                            if (collectedKeys.add(key)) {
                                List<Object> cached = wrapHTMLFromCache(v, "desc", count, customCSS);
                                if (cached != null) {
                                    pendingNodes.put(key, cached);
                                } else {
                                    fieldSpecs.add(new String[] { key, "desc",
                                            this.replaceFigureVariables(getDescription(v), count) });
                                }
                            }
                        }
                        if (xml.contains("${details}")) {
                            String key = "${details:" + count + "}";
                            nxml = StringUtils.replace(nxml, "${details}", key);
                            rowKeys.add(key);
                            if (collectedKeys.add(key)) {
                                List<Object> cached = wrapHTMLFromCache(v, "details", count, customCSS);
                                if (cached != null) {
                                    pendingNodes.put(key, cached);
                                } else {
                                    fieldSpecs.add(new String[] { key, "details",
                                            this.replaceFigureVariables(getDetails(v), count) });
                                }
                            }
                        }
                        if (v.getCustomFields() != null) {
                            for (CustomField cf : v.getCustomFields()) {
                                if (cf.getType().getFieldType() == 3
                                        && xml.contains("${cf" + cf.getType().getVariable() + "}")) {
                                    String key = "${cf" + cf.getType().getVariable() + ":" + count + "}";
                                    nxml = StringUtils.replace(nxml,
                                            "${cf" + cf.getType().getVariable() + "}", key);
                                    rowKeys.add(key);
                                    if (collectedKeys.add(key)) {
                                        List<Object> cfCached = wrapHTMLFromCache(v,
                                                "cf:" + cf.getType().getVariable(), count, customCSS);
                                        if (cfCached != null) {
                                            pendingNodes.put(key, cfCached);
                                        } else {
                                            fieldSpecs.add(new String[] { key, cf.getType().getVariable(),
                                                    cf.getValue() == null ? "" : cf.getValue() });
                                        }
                                    }
                                }
                            }
                        }

                        Tr newrow = (Tr) XmlUtils.unmarshalString(nxml);

                        // Replace Hyperlinks
                        if (v.getCustomFields() != null) {
                            for (CustomField cf : v.getCustomFields()) {
                                this.replaceHyperlink(newrow, "${cf" + cf.getType().getVariable() + " link}",
                                        cf.getValue());
                            }
                        }
                        this.replaceHyperlink(newrow, "${cvssString link}", v.getCvssString());

                        ((Tbl) table).getContent().add(newrow);
                        if (!rowKeys.isEmpty()) {
                            pendingRows.add(new Object[] { newrow, rowKeys });
                        }
                    }
                    count++;
                }

                // batch-convert the cache misses, then fill each row. A key
                // can appear in more than one row of the same vulnerability
                // (multi-row loop templates); the first row consumes the
                // converted nodes, later rows insert deep copies — the same
                // JAXB nodes must never sit in the tree twice.
                if (!pendingRows.isEmpty()) {
                    pendingNodes.putAll(convertFieldsBatched(fieldSpecs, customCSS));
                    HashMap<String, List<Object>> insertedNodes = new HashMap<>();
                    for (Object[] pending : pendingRows) {
                        Tr row = (Tr) pending[0];
                        @SuppressWarnings("unchecked")
                        List<String> rowKeys = (List<String>) pending[1];
                        HashMap<String, List<Object>> rowMap = new HashMap<>();
                        for (String key : rowKeys) {
                            List<Object> nodes = pendingNodes.remove(key);
                            if (nodes == null) {
                                List<Object> used = insertedNodes.get(key);
                                if (used != null) {
                                    nodes = deepCopyAll(used);
                                }
                            }
                            if (nodes != null) {
                                rowMap.put(key, nodes);
                                insertedNodes.put(key, nodes);
                            }
                        }
                        replaceHTML(row, rowMap);
                    }
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

            // Assessment-level variable replacement runs BEFORE the findings
            // are inserted: replacementText marshals the entire main document
            // part, which is only the template here but 500+ findings big
            // afterwards. Assessment variables inside finding content are
            // resolved by the per-field paths — replacement() for live
            // conversion, applyAssessmentVarsToXml for the pre-compiled
            // cache — which cover the same variable set.
            HashMap<String, List<Object>> map = new HashMap();

            map.put("${summary1}",
                    this.wrapHTML(this.assessment.getSummary() == null ? "" : this.assessment.getSummary(), customCSS,
                            "summary1"));
            map.put("${summary2}",
                    this.wrapHTML(this.assessment.getRiskAnalysis() == null ? "" : this.assessment.getRiskAnalysis(),
                            customCSS, "summary2"));
            replaceHTML(mlp.getMainDocumentPart(), map, false);
            replaceAssessment(customCSS);

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

            insertPageBreaks();
            updateDocWithExtensions(customCSS);

            // release the per-report caches — the document is about to be
            // serialized and converted downstream, which is when heap
            // pressure peaks
            nodeMap.clear();
            imageCache.clear();
            imageCacheChars = 0;
            imagesNotFound.clear();
            warmedImages.clear();
            inlineImagesDownscaled.clear();
            embeddedImageRIdCache.clear();
            templateTables = null;
            processedTables.clear();

            return mlp;
        } finally {
            context.end();
        }
    }

    // marker paragraph used to split one batched XHTML conversion back into
    // its individual fields
    private static final String FIELD_SPLIT_MARKER = "FCT-FIELD-SPLIT-7f3a91";
    // batch limits: enough fields per conversion to amortize the importer's
    // fixed cost (page parse, CSS matcher build, layout setup), small enough
    // to keep single-conversion memory bounded
    private static final int MAX_CHUNK_FIELDS = 25;
    private static final int MAX_CHUNK_CHARS = 2 * 1024 * 1024;

    // FACTION_REPORT_BATCH_CONVERT=false forces one conversion per field
    // (the pre-batching behavior) — an isolation lever for rendering issues
    private static boolean batchConvertEnabled() {
        String conf = System.getProperty("FACTION_REPORT_BATCH_CONVERT");
        if (conf == null || conf.trim().isEmpty()) {
            conf = System.getenv("FACTION_REPORT_BATCH_CONVERT");
        }
        return conf == null || !conf.trim().equalsIgnoreCase("false");
    }

    private String preprocessHTMLContent(String content) {
        if (!content.isEmpty()) {
            content = replacement(content);
            // fix extra spaces
            content = content.replaceAll("\n", "<br />");
            // content = content.replaceAll("</p><p><br /></p><p>", "<br
            // /></p><p>");//replace extra space
            content = content.replaceAll("<p><br /></p>", "");// replace extra space
            content = content.replaceAll("<blockquote>", "<center class='figure'>");
            content = content.replaceAll("</blockquote>", "</center>");
        }
        return content;
    }

    private String htmlPageHead(String customCSS) {
        return "<!DOCTYPE html><html><head>"
                + "<style>html{padding:0;margin:0;margin-right:0px;}\r\nbody{padding:0;margin:0;font-family:"
                + this.FONT + ";}\r\n" + customCSS + "</style>" + "</head><body>";
    }

    private XHTMLImporterImpl newImporter() {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "newImporter");
        try{
            LoggingConfig.configureOpenHTMLTopDFLogging();
            XHTMLImporterImpl xhtml = new XHTMLImporterImpl(mlp);
            RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
            rfonts.setAscii(this.FONT);
            XHTMLImporterImpl.addFontMapping("Arial", rfonts);
            XHTMLImporterImpl.addFontMapping("arial", rfonts);
            return xhtml;
         } finally {
                context.end();
         }
    }

    // ===== pre-compiled cache fast path =====

    /**
     * Tries to serve wrapHTML from a pre-compiled cache stored on the
     * Vulnerability at save time (by DocxPrecompiler). The cached XML
     * has assessment-level ${...} variables as literal text inside <w:t>
     * nodes — this method resolves them via string replacement on the
     * XML, then unmarshals to JAXB nodes.
     *
     * Falls back to live conversion (returns null sentinel) when:
     * - cache is null or hash-stale (a CACHE_VERSION bump in the hash
     *   automatically invalidates every older-format row)
     * - unmarshalling fails (malformed cache)
     * - numbering tokens can't all be resolved
     *
     * Image data URIs in the cached XML become real image parts in the
     * report package (resolveEmbeddedImages); cached numbering definitions
     * are re-created under freshly allocated ids (spliceCachedNumbering)
     * so list formats survive the move between packages.
     *
     * @param v        the vulnerability whose cached field to use
     * @param field    "desc", "rec", or "details"
     * @param count    1-based vulnerability index for figure numbering
     * @return converted JAXB nodes, or null to fall back to live wrapHTML
     */
    @ProfileMethod("wrapHTML from pre-compiled cache")
    private List<Object> wrapHTMLFromCache(Vulnerability v, String field, int count, String customCSS) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "wrapHTMLFromCache");
        try {
            String cachedXml;
            String cachedHash;
            String preContent; // content BEFORE replaceFigureVariables

            switch (field) {
                case "desc":
                    cachedXml = v.getCachedDescXml();
                    cachedHash = v.getCachedDescHash();
                    preContent = getDescription(v);
                    break;
                case "rec":
                    cachedXml = v.getCachedRecXml();
                    cachedHash = v.getCachedRecHash();
                    preContent = getRecommendation(v);
                    break;
                case "details":
                    cachedXml = v.getCachedDetailsXml();
                    cachedHash = v.getCachedDetailsHash();
                    preContent = v.getDetails() != null ? v.getDetails() : "";
                    preContent = replaceAllCf(preContent, v);
                    break;
                default:
                    // "cf:<variable>" — a type-3 (HTML) custom field, cached
                    // as a marker-delimited entry with its own hash
                    if (!field.startsWith("cf:")) {
                        return null;
                    }
                    String cfVar = field.substring(3);
                    String cfValue = null;
                    if (v.getCustomFields() != null) {
                        for (CustomField cf : v.getCustomFields()) {
                            if (cf.getType() != null && cf.getType().getFieldType() == 3
                                    && cfVar.equals(cf.getType().getVariable())) {
                                cfValue = cf.getValue();
                                break;
                            }
                        }
                    }
                    if (cfValue == null || cfValue.isEmpty()) {
                        return null;
                    }
                    String[] entry = DocxPrecompiler.findCfEntry(v.getCachedCfXml(), cfVar);
                    if (entry == null) {
                        return null;
                    }
                    cachedHash = entry[0];
                    cachedXml = entry[1];
                    preContent = cfValue;
                    break;
            }

            if (cachedXml == null || cachedHash == null) {
                return null;
            }

            String expectedHash = DocxPrecompiler.contentHash(this.FONT, preContent);
            if (!expectedHash.equals(cachedHash)) {
                return null;
            }

            // split the payload into field XML and its numbering definitions
            String fieldXml = cachedXml;
            String numberingXml = null;
            int marker = cachedXml.indexOf(DocxPrecompiler.NUM_DEFS_MARKER);
            if (marker >= 0) {
                fieldXml = cachedXml.substring(0, marker);
                numberingXml = cachedXml.substring(marker + DocxPrecompiler.NUM_DEFS_MARKER.length());
            }

            // safety net: numbering references without carried definitions
            // can only come from a pre-v7 cache (bare scratch-package numIds
            // that resolve against the template's numbering — the bug that
            // rendered bullets as continued decimal numbers). The version
            // bump already invalidates those via the hash; never trust one.
            if (numberingXml == null && fieldXml.contains("w:numId")) {
                return null;
            }

            String xml = fieldXml;

            // Variable substitution runs dozens of full scans over the
            // field XML (which can be megabytes with embedded images), so
            // it is gated on the tokens actually being present — base64
            // payloads never contain "${" or "{[", so one indexOf each is
            // a reliable test. Most fields have neither.
            if (xml.indexOf("${") >= 0) {
                // assessment-level ${asmtName} etc. survive as literal text
                // inside <w:t> nodes because the precompiler didn't resolve
                // them; figure variables: ${Figure#.X} → Figure count.X
                xml = applyAssessmentVarsToXml(xml);
                xml = replaceFigureVariables(xml, count);
            }
            if (xml.indexOf("{[asmt") >= 0) {
                // {[asmtSEVERITY]} ranked lists (assessment-scoped)
                xml = loopReplace(xml);
            }

            // resolve embedded image data URIs to real image parts in the
            // report package. The precompiler embedded "data:image/..." as
            // the r:embed value — we create BinaryParts and replace with
            // valid rIds so Word can display the images
            xml = resolveEmbeddedImages(xml);

            // re-create the field's numbering definitions in the report
            // package and swap the FCT-NUM tokens for the fresh ids
            if (numberingXml != null) {
                xml = spliceCachedNumbering(xml, numberingXml);
                if (xml == null || xml.contains("FCT-NUM-")) {
                    return null; // unresolved token — fall back to live
                }
            }

            // unmarshal the XML string back to JAXB nodes. The cached XML
            // is a concatenation of <w:p> elements — unmarshal each one
            // individually to avoid deep JAXB call stacks on large snippets
            List<Object> nodes = new ArrayList<>();
            try {
                List<String> snippets = splitTopLevelParagraphs(xml);
                if (snippets.isEmpty()) {
                    // the cached XML doesn't contain recognizable <w:p>
                    // elements — fall back to live conversion rather than
                    // returning an empty list.
                    return null;
                }
                for (String snippet : snippets) {
                    Object node = XmlUtils.unmarshalString(snippet);
                    nodes.add(node);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null; // fall back to live
            }

            if (nodes.isEmpty()) {
                return null; // nothing unmarshalled — fall back to live
            }

            // returned directly, not through nodeMap: deepCopyAll is a
            // marshal+unmarshal round trip per node, so storing a shared
            // copy and cloning it costs more than the unmarshal above —
            // duplicate content just deserializes again. This also keeps
            // numbering instances per-field (sharing nodes across fields
            // would make identical numbered lists continue counting).
            return nodes;
        } finally {
            context.end();
        }
    }

    /**
     * Re-creates a cached field's numbering definitions inside the report
     * package and rewrites the field XML's FCT-NUM tokens to the freshly
     * allocated ids.
     *
     * Every cached abstractNum/num gets its own new instance — never
     * deduplicated or shared. The importer creates one abstractNum per
     * list, and that per-list identity is exactly what makes numbered
     * lists restart at 1 for each finding; sharing an abstract across
     * instances would make Word continue the count across findings.
     *
     * @return the field XML with tokens substituted, or null on failure
     */
    private String spliceCachedNumbering(String xml, String numberingXml) {
        try {
            Numbering cached = (Numbering) XmlUtils.unmarshalString(numberingXml);
            NumberingDefinitionsPart ndp = mlp.getMainDocumentPart().getNumberingDefinitionsPart();
            if (ndp == null) {
                ndp = new NumberingDefinitionsPart();
                ndp.setJaxbElement(Context.getWmlObjectFactory().createNumbering());
                mlp.getMainDocumentPart().addTargetPart(ndp);
            }
            Numbering target = ndp.getContents();

            long maxAbs = 0;
            for (Numbering.AbstractNum a : target.getAbstractNum()) {
                if (a.getAbstractNumId() != null) {
                    maxAbs = Math.max(maxAbs, a.getAbstractNumId().longValue());
                }
            }
            long maxNum = 0;
            for (Numbering.Num n : target.getNum()) {
                if (n.getNumId() != null) {
                    maxNum = Math.max(maxNum, n.getNumId().longValue());
                }
            }

            Map<java.math.BigInteger, java.math.BigInteger> absMap = new HashMap<>();
            for (Numbering.AbstractNum a : cached.getAbstractNum()) {
                Numbering.AbstractNum copy = XmlUtils.deepCopy(a);
                java.math.BigInteger fresh = java.math.BigInteger.valueOf(++maxAbs);
                absMap.put(a.getAbstractNumId(), fresh);
                copy.setAbstractNumId(fresh);
                target.getAbstractNum().add(copy);
            }
            for (Numbering.Num n : cached.getNum()) {
                Numbering.Num copy = XmlUtils.deepCopy(n);
                java.math.BigInteger fresh = java.math.BigInteger.valueOf(++maxNum);
                copy.setNumId(fresh);
                if (copy.getAbstractNumId() != null
                        && absMap.containsKey(copy.getAbstractNumId().getVal())) {
                    copy.getAbstractNumId().setVal(absMap.get(copy.getAbstractNumId().getVal()));
                }
                target.getNum().add(copy);
                xml = xml.replace("w:val=\"FCT-NUM-" + n.getNumId() + "\"", "w:val=\"" + fresh + "\"");
            }
            return xml;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // applies assessment-level ${...} variable substitutions on raw XML
    // text. The cached XML has these as literal text inside <w:t> nodes.
    // CDATA-wrapping matches what replacementText() does for the document-
    // wide pass.
    private String applyAssessmentVarsToXml(String xml) {
        xml = StringUtils.replace(xml, "${asmtName}",
                CData(this.assessment.getName() == null ? "" : this.assessment.getName()));
        xml = StringUtils.replace(xml, "${asmtId}",
                CData(this.assessment.getId() == null ? "" : "" + this.assessment.getId()));
        xml = StringUtils.replace(xml, "${asmtAppId}", CData("" + this.assessment.getAppId()));
        xml = StringUtils.replace(xml, "${asmtAssessor}", CData(this.assessment.getAssessor() == null ? ""
                : (this.assessment.getAssessor().get(0).getFname() + " "
                        + this.assessment.getAssessor().get(0).getLname())));
        xml = StringUtils.replace(xml, "${asmtAssessor_Email}",
                CData(this.assessment.getAssessor() == null ? "" : (this.assessment.getAssessor().get(0).getEmail())));
        xml = StringUtils.replace(xml, "${asmtAssessors_Lines}",
                CData(this.assessment.getAssessor() == null ? "" : this.assessorsLines));
        xml = StringUtils.replace(xml, "${asmtAssessors_Comma}",
                CData(this.assessment.getAssessor() == null ? "" : this.assessorsComma));
        xml = StringUtils.replace(xml, "${asmtAssessors_Bullets}",
                CData(this.assessment.getAssessor() == null ? "" : this.assessorsBullets));
        xml = StringUtils.replace(xml, "${remediation}", CData(this.assessment.getRemediation() == null ? ""
                : (this.assessment.getRemediation().getFname() + " " + this.assessment.getRemediation().getLname())));
        xml = StringUtils.replace(xml, "${asmtTeam}", CData(this.assessment.getAssessor() == null ? ""
                : this.assessment.getAssessor().get(0).getTeam() == null ? ""
                        : this.assessment.getAssessor().get(0).getTeam().getTeamName().trim()));
        xml = StringUtils.replace(xml, "${asmtType}",
                CData(this.assessment.getType() == null ? "" : this.assessment.getType().getType().trim()));
        xml = replaceDateVariable(xml, "today", new Date());
        xml = replaceDateVariable(xml, "asmtStart", this.assessment.getStart());
        xml = replaceDateVariable(xml, "asmtEnd", this.assessment.getEnd());
        xml = StringUtils.replace(xml, "${asmtAccessKey}", CData(this.assessment.getGuid()));
        xml = StringUtils.replace(xml, "${totalOpenVulns}", CData(this.totalOpenVulns));
        xml = StringUtils.replace(xml, "${totalClosedVulns}", CData(this.totalClosedVulns));
        // risk counts
        if (this.vulns != null) {
            for (int i = 0; i < 10; i++) {
                xml = StringUtils.replace(xml, "${riskCount" + i + "}", CData("" + riskCounts[i]));
            }
            xml = StringUtils.replace(xml, "${riskTotal}", CData("" + riskTotal));
        }
        // assessment-level text custom fields — with replaceAssessment now
        // running before the findings are inserted, ${cfX} tokens inside
        // finding content must be resolved here
        if (this.assessment.getCustomFields() != null) {
            for (CustomField cf : this.assessment.getCustomFields()) {
                if (cf.getType() != null && cf.getType().getFieldType() < 3) {
                    xml = StringUtils.replace(xml, "${cf" + cf.getType().getVariable() + "}",
                            CData(cf.getValue() == null ? "" : cf.getValue()));
                }
            }
        }
        return xml;
    }

    // per-report dedup: data URI → rId for images already created in the
    // report package. The same screenshot embedded in multiple vulns
    // (or desc+details of the same vuln) gets one image part, not many.
    private Map<String, String> embeddedImageRIdCache = new HashMap<>();

    // Pattern to find r:embed="data:image/..." or r:link="data:image/..."
    // in cached XML. The precompiler embedded the full data URI as the
    // embed value so the cached XML is self-contained.
    private static final Pattern EMBEDDED_IMAGE_PATTERN = Pattern.compile(
            "(r:embed|r:link)=\"(data:image/[^\"]+)\"");

    /**
     * Scans cached XML for embedded image data URIs (placed by the
     * precompiler), creates real image parts in the report package, and
     * replaces the data URIs with valid rId references. Deduplicates
     * identical images across vulns/fields within a single report.
     */
    private String resolveEmbeddedImages(String xml) {
        if (!xml.contains("data:image/")) return xml;

        Matcher m = EMBEDDED_IMAGE_PATTERN.matcher(xml);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String attr = m.group(1);
            String dataUri = m.group(2);
            String rId = embeddedImageRIdCache.get(dataUri);
            if (rId == null) {
                try {
                    // decode the data URI and create an image part in the
                    // report package
                    String[] uriParts = dataUri.substring(5).split(",", 2);
                    byte[] bytes = Base64.getDecoder().decode(uriParts[1]);
                    String mime = uriParts[0].split(";")[0];
                    // known formats get the part created directly — the
                    // probing path below re-parses every image to determine
                    // format and dimensions, which the cached drawing XML
                    // already carries from precompile time
                    rId = addEmbeddedImagePart(bytes, mime);
                    if (rId == null) {
                        // unknown format: let docx4j probe it
                        org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage imagePart =
                            org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
                                .createImagePart(mlp, bytes);
                        org.docx4j.relationships.Relationship rel = imagePart.getRelLast();
                        if (rel != null) {
                            rId = rel.getId();
                        }
                        if (rId == null) {
                            // fallback: find the rId from the main document
                            // part's relationships to this image part
                            rId = mlp.getMainDocumentPart().getRelationshipsPart()
                                .getRelationships().getRelationship().stream()
                                .filter(r -> r.getTarget().contains(imagePart.getPartName().getName()))
                                .map(org.docx4j.relationships.Relationship::getId)
                                .reduce((first, second) -> second)
                                .orElse(null);
                        }
                    }
                    if (rId != null) {
                        embeddedImageRIdCache.put(dataUri, rId);
                    } else {
                        // couldn't find the rId — remove the reference
                        m.appendReplacement(sb, Matcher.quoteReplacement(attr + "=\"\""));
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    m.appendReplacement(sb, Matcher.quoteReplacement(attr + "=\"\""));
                    continue;
                }
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(attr + "=\"" + rId + "\""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // sequence for unique media part names; "fctimage" avoids clashing with
    // the template's own image1.png-style names
    private int embeddedImagePartCounter = 0;

    // Creates an image part for a known format without docx4j's
    // createImagePart() probe (which decodes every image to determine
    // format and dimensions). Returns the relationship id, or null for
    // formats the caller should hand to the probing path.
    private String addEmbeddedImagePart(byte[] bytes, String mime) {
        try {
            org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage part;
            if (mime.contains("png")) {
                part = new org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart(
                        new org.docx4j.openpackaging.parts.PartName(
                                "/word/media/fctimage" + (++embeddedImagePartCounter) + ".png"));
            } else if (mime.contains("jpeg") || mime.contains("jpg")) {
                part = new org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart(
                        new org.docx4j.openpackaging.parts.PartName(
                                "/word/media/fctimage" + (++embeddedImagePartCounter) + ".jpeg"));
            } else if (mime.contains("gif")) {
                part = new org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart(
                        new org.docx4j.openpackaging.parts.PartName(
                                "/word/media/fctimage" + (++embeddedImagePartCounter) + ".gif"));
            } else {
                return null;
            }
            part.setBinaryData(bytes);
            org.docx4j.relationships.Relationship rel = mlp.getMainDocumentPart().addTargetPart(part);
            return rel == null ? null : rel.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // splits a concatenated XML string into individual top-level paragraph
    // elements, so each can be unmarshaled independently. This avoids deep
    // JAXB call stacks when unmarshaling large snippets.
    //
    // The docx4j marshaller may use the standard <w:p> prefix when
    // marshalling inside a document context, or an arbitrary namespace
    // prefix (e.g. <ns3:p>) when marshalling a standalone node. This
    // method handles both by falling back to a regex-based split if the
    // standard prefix scan finds nothing.
    private static List<String> splitTopLevelParagraphs(String xml) {
        List<String> parts = new ArrayList<>();
        int scan = 0;
        while (scan < xml.length()) {
            int pStart = xml.indexOf("<w:p ", scan);
            int pStartPlain = xml.indexOf("<w:p>", scan);
            int best;
            if (pStart < 0) {
                best = pStartPlain;
            } else if (pStartPlain < 0) {
                best = pStart;
            } else {
                best = Math.min(pStart, pStartPlain);
            }
            if (best < 0) {
                break;
            }
            // find matching </w:p> with depth tracking for nested w:p
            int depth = 0;
            int i = best + 4;
            int end = -1;
            while (i < xml.length()) {
                int nextOpen = xml.indexOf("<w:p ", i);
                int nextOpenPlain = xml.indexOf("<w:p>", i);
                int nextClose = xml.indexOf("</w:p>", i);
                int bestOpen;
                if (nextOpen < 0) {
                    bestOpen = nextOpenPlain;
                } else if (nextOpenPlain < 0) {
                    bestOpen = nextOpen;
                } else {
                    bestOpen = Math.min(nextOpen, nextOpenPlain);
                }
                if (nextClose < 0) break;
                if (bestOpen >= 0 && bestOpen < nextClose) {
                    depth++;
                    i = bestOpen + 5;
                } else {
                    if (depth == 0) {
                        end = nextClose + "</w:p>".length();
                        break;
                    }
                    depth--;
                    i = nextClose + "</w:p>".length();
                }
            }
            if (end < 0) break;
            String snippet = xml.substring(best, end);
            if (!snippet.contains("xmlns:w=")) {
                snippet = snippet.replaceFirst("<w:p",
                        "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
                        + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
                        + " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
                        + " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
                        + " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"");
            }
            parts.add(snippet);
            scan = end;
        }

        // fallback: if no <w:p> elements were found, the marshaller may
        // have used a different namespace prefix (e.g. <ns3:p>). Try a
        // regex-based split on any element whose local name is "p".
        if (parts.isEmpty() && xml.contains(":p") && xml.contains(":p>")) {
            Pattern altPPattern = Pattern.compile(
                "<\\w+:p[ >].*?</\\w+:p>",
                Pattern.DOTALL);
            Matcher m = altPPattern.matcher(xml);
            while (m.find()) {
                String snippet = m.group();
                // normalize the namespace prefix to w: so XmlUtils can
                // unmarshal it in the standard wordprocessingml context
                String prefix = snippet.substring(1, snippet.indexOf(":p"));
                snippet = snippet.replace(prefix + ":p", "w:p");
                if (!snippet.contains("xmlns:w=")) {
                    snippet = snippet.replaceFirst("<w:p",
                            "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
                            + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
                            + " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
                            + " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
                            + " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"");
                }
                parts.add(snippet);
            }
        }

        return parts;
    }

    @ProfileMethod("Convert HTML content to docx format with CSS styling")
    private List<Object> wrapHTML(String content, String customCSS,
            String className) throws Docx4JException{
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "wrapHTML");
        try {

            if (className == null) {
                className = "";
            }

            // cache on the raw input: identical content converts to identical
            // nodes, and skipping straight past replacement()/jtidy/conversion
            // is the whole win. Copies are returned because the same JAXB
            // nodes must not be inserted into the document tree twice.
            String cacheKey = className + " " + content;
            List<Object> cached = nodeMap.get(cacheKey);
            if (cached != null) {
                return deepCopyAll(cached);
            }

            content = preprocessHTMLContent(content);
			String htmlPrefix = htmlPageHead(customCSS) + "<div class='" + className + "'>";
			String htmlSuffix = "</div></body></html>";
			String html = htmlPrefix + content + htmlSuffix;

			XHTMLImporterImpl xhtml = newImporter();
			MethodProfiler.ProfileContext convertContext = MethodProfiler.start("DocxUtils", "xhtmlConvert");
			try {
				List<Object> converted = xhtml.convert(html, null);
				nodeMap.put(cacheKey, converted);
				return converted;
				// jtidy is expensive, so the content is only repaired when
				// the importer actually rejects it
            }catch(Docx4JException ex){
            	// tidy just the fragment and rebuild the page around it —
            	// jtidy outputs body-only, so tidying the whole page would
            	// drop the <style> block
            	String sanitized = htmlPrefix + FSUtils.jtidy(content) + htmlSuffix;
				List<Object> converted;
				try {
					converted = xhtml.convert(sanitized, null);
					nodeMap.put(cacheKey, converted);
					return converted;
				} catch (Docx4JException e) {
					System.out.println(html);
					e.printStackTrace();
					throw(e);
				}
            } finally {
                convertContext.end();
            }
        } finally {
            context.end();
        }
    }

    /**
     * Converts many HTML fields with as few XHTML import passes as possible.
     * Each importer call pays a fixed cost (page/CSS parse, selector matcher,
     * layout setup) that dwarfs a small field's own conversion time, so fields
     * are joined into one page separated by marker paragraphs and the
     * converted nodes are split back apart on those markers.
     *
     * @param fields each entry is {placeholderKey, className, rawContent}
     * @return placeholderKey -> converted nodes, ready for replaceHTML
     */
    @ProfileMethod("Batched XHTML conversion")
    private HashMap<String, List<Object>> convertFieldsBatched(List<String[]> fields, String customCSS)
            throws Docx4JException {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "convertFieldsBatched");
        try {
            // fetch + downscale all referenced images on multiple cores
            // before the (serial) conversions start
            warmImageCache(fields);

            HashMap<String, List<Object>> out = new HashMap<>();
            List<String[]> chunk = new ArrayList<>();
            long chunkChars = 0;
            for (String[] field : fields) {
                // identical content (e.g. hundreds of empty ${details}) is
                // served straight from the cache
                List<Object> cached = nodeMap.get(field[1] + " " + field[2]);
                if (cached != null) {
                    out.put(field[0], deepCopyAll(cached));
                    continue;
                }
                chunk.add(field);
                chunkChars += field[2].length();
                if (chunk.size() >= MAX_CHUNK_FIELDS || chunkChars >= MAX_CHUNK_CHARS) {
                    convertChunk(chunk, customCSS, out);
                    chunk = new ArrayList<>();
                    chunkChars = 0;
                }
            }
            if (!chunk.isEmpty()) {
                convertChunk(chunk, customCSS, out);
            }
            return out;
        } finally {
            context.end();
        }
    }

    private void convertChunk(List<String[]> chunk, String customCSS, HashMap<String, List<Object>> out)
            throws Docx4JException {
        if (chunk.size() > 1 && batchConvertEnabled()) {
            try {
                // preprocess each field (variable replacement, jtidy, images)
                // exactly as the individual path does
                List<String> processed = new ArrayList<>(chunk.size());
                for (String[] field : chunk) {
                    processed.add(preprocessHTMLContent(field[2]));
                }

                StringBuilder html = new StringBuilder(htmlPageHead(customCSS));
                for (int i = 0; i < chunk.size(); i++) {
                    html.append("<p>").append(FIELD_SPLIT_MARKER).append("</p>");
                    html.append("<div class='").append(chunk.get(i)[1]).append("'>")
                            .append(processed.get(i)).append("</div>");
                }
                html.append("</body></html>");

                XHTMLImporterImpl xhtml = newImporter();
                MethodProfiler.ProfileContext convertContext = MethodProfiler.start("DocxUtils", "xhtmlConvertBatch");
                List<Object> converted;
                try {
                    converted = xhtml.convert(html.toString(), null);
                } finally {
                    convertContext.end();
                }

                // split the converted nodes back into per-field segments
                List<List<Object>> segments = new ArrayList<>();
                List<Object> current = null;
                for (Object node : converted) {
                    if (isSplitMarker(node)) {
                        current = new ArrayList<>();
                        segments.add(current);
                    } else if (current != null) {
                        current.add(node);
                    }
                }
                if (segments.size() != chunk.size()) {
                    throw new Docx4JException("field split marker mismatch: expected "
                            + chunk.size() + " segments, found " + segments.size());
                }
                for (int i = 0; i < chunk.size(); i++) {
                    String[] field = chunk.get(i);
                    out.put(field[0], segments.get(i));
                    nodeMap.put(field[1] + " " + field[2], segments.get(i));
                }
                return;
            } catch (Exception ex) {
                // anything unexpected (marker collision with user content,
                // malformed field poisoning the chunk) falls back to the
                // battle-tested one-field-at-a-time path below
                System.err.println("Batched HTML conversion failed, converting fields individually: "
                        + ex.getMessage());
            }
        }

        for (String[] field : chunk) {
            out.put(field[0], wrapHTML(field[2], customCSS, field[1]));
        }
    }

    private boolean isSplitMarker(Object node) {
        Object unwrapped = XmlUtils.unwrap(node);
        if (!(unwrapped instanceof P)) {
            return false;
        }
        StringWriter text = new StringWriter();
        try {
            TextUtils.extractText(unwrapped, text);
        } catch (Exception ex) {
            return false;
        }
        return FIELD_SPLIT_MARKER.equals(text.toString().trim());
    }

    private static List<Object> deepCopyAll(List<Object> nodes) {
        List<Object> copies = new ArrayList<>(nodes.size());
        for (Object node : nodes) {
            copies.add(XmlUtils.deepCopy(node));
        }
        return copies;
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
            // dates are applied inside the same marshal/unmarshal pass as the
            // plain-text variables in replacementText below
            Map<String, Date> dateMap = new LinkedHashMap<>();
            dateMap.put("today", new Date());
            dateMap.put("asmtStart", this.assessment.getStart());
            dateMap.put("asmtEnd", this.assessment.getEnd());
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
    		replacementText(map, dateMap);

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
        // counts are precomputed once per report
        if (this.vulns != null) {
            for (int i = 0; i < 10; i++) {
                content = content.replaceAll("\\$\\{riskCount" + i + "\\}", "" + riskCounts[i]);
            }
            content = content.replaceAll("\\$\\{riskTotal\\}", "" + riskTotal);
        }
        return content;
    }

    private Map<String, String> getVulnMap() {
        Map<String, String> maps = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            maps.put("riskCount" + i + "", "" + riskCounts[i]);

        }
        maps.put("riskTotal", "" + riskTotal);
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

    // Replace simple text and date variables in a single marshal/unmarshal
    // pass over the document
    private void replacementText(Map<String, String> map, Map<String, Date> dates)
            throws JAXBException, Docx4JException {
        String xml = XmlUtils.marshaltoString(mlp.getMainDocumentPart().getContents(), false, false);
        for (Map.Entry<String, Date> date : dates.entrySet()) {
            xml = replaceDateVariable(xml, date.getKey(), date.getValue());
        }
        for (String key : map.keySet()) {
        	xml = replaceAll(
        			xml,
        			key,
        			""+ map.get(key) == null ? "" : map.get(key),
        			true);
        }
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
			SimpleDateFormat formatter = DATE_FORMAT.get();

			// assessor strings are precomputed once per report
			String assessors_nl = this.assessorsLines;
			String assessors_comma = this.assessorsComma;
			String assessors_bullets = this.assessorsBullets;

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
			content = replaceDateVariable(content, "asmtStart", this.assessment.getStart());
			content = replaceDateVariable(content, "asmtEnd", this.assessment.getEnd());
			content = content.replaceAll("\\$\\{asmtStandND\\}", formatter.format(this.assessment.getEnd()));
			content = content.replaceAll("\\$\\{asmtAccessKey\\}", this.assessment.getGuid());
			content = content.replaceAll("\\$\\{totalOpenVulns\\}", this.totalOpenVulns);
			content = content.replaceAll("\\$\\{totalClosedVulns\\}", this.totalClosedVulns);
			// assessment-level text custom fields — with replaceAssessment
			// running before the findings are inserted, ${cfX} tokens inside
			// finding content must be resolved here
			if (this.assessment.getCustomFields() != null) {
				for (CustomField acf : this.assessment.getCustomFields()) {
					if (acf.getType() != null && acf.getType().getFieldType() < 3) {
						content = StringUtils.replace(content, "${cf" + acf.getType().getVariable() + "}",
								acf.getValue() == null ? "" : acf.getValue());
					}
				}
			}


			// Run extensions — only when the content contains a ${ placeholder
			// that an extension might handle. This avoids the expensive
			// full-assessment clone in updateReport() for the 1340+ fields
			// that have no extension work. Extensions use ${faction-*} by
			// convention; any extension placeholder will contain "${".
			if (this.reportExtension.isExtended() && content.contains("${")) {
				content = this.reportExtension.updateReport(this.assessment, content);
			}

			content = loopReplace(content);

			// Fix any html weirdness. This must run BEFORE images are inlined
			// as base64 — repairing content afterwards means tidying megabytes
			// of embedded image data per field
			content = FSUtils.jtidy(content);
			// editors emit a <ul> directly inside an <ol> when the list type
			// changes; jtidy wraps it in <li style="list-style: none"> which
			// the XHTML importer numbers with the parent list's format
			// (bullets render as 4,5,6...). Hoist it out to a sibling list.
			content = hoistMisnestedLists(content);
			// Downscale oversized images that are already inline in the
			// content (legacy/imported data); getImage links are downscaled
			// when they are resolved
			if (maxImageWidth > 0 && content.contains("data:image/")) {
				content = downscaleInlineImages(content);
			}
			// Fix images
			content = this.replaceImageLinks(content);
			return content;
        }finally {
        	context.end();
        }

    }

    private static final Pattern LIST_TAG_PATTERN = Pattern.compile("<(/?)(ol|ul|li)\\b[^>]*>",
            Pattern.CASE_INSENSITIVE);

    /**
     * Moves a list wrapped in an otherwise-empty {@code <li>} out to be a
     * sibling of the enclosing list. Editors emit a list nested directly
     * inside another list when the list type changes mid-list; the HTML
     * sanitizer (on save) and jtidy both repair that by wrapping the inner
     * list in its own {@code <li>}. Browsers render such a wrapper as an
     * independent list, but the XHTML importer numbers it with the enclosing
     * list's format — turning bullets into continued numbering. A wrapper
     * {@code <li>} is one whose entire content is the nested list;
     * intentional sublists ({@code <li>text<ul>...}) are left untouched.
     */
    static String hoistMisnestedLists(String content) {
        if (content.indexOf("<li") == -1) {
            return content;
        }
        // a hoist can expose another wrapper (nested repairs) — iterate
        for (int pass = 0; pass < 20; pass++) {
            Matcher m = LIST_TAG_PATTERN.matcher(content);
            // stack entries: {tagName, openTagStart, openTagEnd}
            java.util.Deque<Object[]> stack = new java.util.ArrayDeque<>();
            int wrapperStart = -1;
            int wrapperContentStart = -1;
            int wrapperContentEnd = -1;
            int wrapperEnd = -1;
            String parentList = null;

            while (m.find()) {
                boolean closing = !m.group(1).isEmpty();
                String tag = m.group(2).toLowerCase();
                if (!closing) {
                    stack.push(new Object[] { tag, m.start(), m.end() });
                } else {
                    // pop to the matching open tag, tolerating unclosed <li>s
                    Object[] open = null;
                    while (!stack.isEmpty()) {
                        Object[] candidate = stack.pop();
                        if (candidate[0].equals(tag)) {
                            open = candidate;
                            break;
                        }
                    }
                    if (open == null) {
                        return content; // unbalanced markup — leave untouched
                    }
                    if (tag.equals("li")) {
                        String inner = content.substring((Integer) open[2], m.start()).trim();
                        boolean wrapsOnlyAList = (inner.startsWith("<ul") || inner.startsWith("<ol"))
                                && (inner.endsWith("</ul>") || inner.endsWith("</ol>"));
                        if (wrapsOnlyAList) {
                            // closed a wrapper li: its parent list is on the stack
                            Object[] parent = null;
                            for (Object[] entry : stack) {
                                if (entry[0].equals("ol") || entry[0].equals("ul")) {
                                    parent = entry;
                                    break;
                                }
                            }
                            if (parent != null) {
                                wrapperStart = (Integer) open[1];
                                wrapperContentStart = (Integer) open[2];
                                wrapperContentEnd = m.start();
                                wrapperEnd = m.end();
                                parentList = (String) parent[0];
                                break;
                            }
                        }
                    }
                }
            }

            if (wrapperStart == -1) {
                return content;
            }
            String inner = content.substring(wrapperContentStart, wrapperContentEnd);
            // close the parent list, emit the hoisted list, reopen the parent
            content = content.substring(0, wrapperStart)
                    + "</" + parentList + ">" + inner + "<" + parentList + ">"
                    + content.substring(wrapperEnd);
            // drop lists left empty by the split (wrapper was first/last item)
            content = content.replaceAll("<(ol|ul)\\b[^>]*>\\s*</\\1>", "");
        }
        return content;
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

            // Remove undefined image links first (quote style may vary now
            // that content is no longer normalized by jtidy)
            String badImage = "<img[^>]*src=[\"']getImage\\?id(=|&#61;)undefined[\"'][^>]*>(</img>)?";
            text = text.replaceAll(badImage, "");

            Pattern imagePattern = IMAGE_LINK_PATTERN;

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

            Map<String, String> resolvedImages = resolveImages(referencedGuids);

            // Second pass: replace or remove image links
            StringBuffer result = new StringBuffer();
            matcher = imagePattern.matcher(text);
            while (matcher.find()) {
                String guid = matcher.group(2);
                String base64Image = resolvedImages.get(guid);

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

    // Resolves the GUIDs referenced by the current content field. Only
    // referenced images are ever loaded (never the assessment's full image
    // collection), repeated references are served from a bounded LRU cache,
    // and unresolvable GUIDs are remembered so they are not re-queried for
    // every field. The returned map holds the current field's images
    // regardless of cache eviction.
    private Map<String, String> resolveImages(Set<String> referencedGuids) {
        Map<String, String> resolved = new HashMap<>();
        List<String> toFetch = new ArrayList<>();
        for (String guid : referencedGuids) {
            String cached = this.warmedImages.get(guid);
            if (cached == null) {
                cached = this.imageCache.get(guid);
            }
            if (cached != null) {
                resolved.put(guid, cached);
            } else if (!this.imagesNotFound.contains(guid)) {
                toFetch.add(guid);
            }
        }
        if (toFetch.isEmpty()) {
            return resolved;
        }

        EntityManager em = HibHelper.getInstance().getEM();
        try {
            for (String guid : toFetch) {
                try {
                    Image img = (Image) em.createQuery("SELECT i FROM Image i WHERE i.guid = :guid")
                            .setParameter("guid", guid)
                            .getSingleResult();
                    if (img != null && img.getBase64Image() != null) {
                        String base64 = ReportImageScaler.reportUri(img, maxImageWidth);
                        resolved.put(guid, base64);
                        cacheImage(guid, base64);
                    } else {
                        this.imagesNotFound.add(guid);
                    }
                } catch (Exception e) {
                    // Image not found for this GUID - will be removed from text
                    this.imagesNotFound.add(guid);
                }
            }
        } finally {
            em.close();
        }
        return resolved;
    }

    // Replaces oversized inline data-URI images with downscaled versions.
    // Results precomputed (in parallel) by warmImageCache are consumed here;
    // anything not warmed is downscaled on the spot.
    private String downscaleInlineImages(String content) {
        List<String> uris = extractInlineDataUris(content);
        if (uris.isEmpty()) {
            return content;
        }
        StringBuilder sb = new StringBuilder(content);
        for (String uri : uris) {
            String downscaled = this.inlineImagesDownscaled.remove(ReportImageScaler.hash(uri));
            if (downscaled == null) {
                downscaled = ReportImageScaler.downscaleDataUri(uri, maxImageWidth);
            }
            if (!downscaled.equals(uri)) {
                int idx = sb.indexOf(uri);
                if (idx >= 0) {
                    sb.replace(idx, idx + uri.length(), downscaled);
                }
            }
        }
        return sb.toString();
    }

    // Finds src attribute values that are data:image URIs. Quote-delimited
    // scan — regex over multi-megabyte base64 payloads is not worth it.
    private static List<String> extractInlineDataUris(String content) {
        List<String> uris = new ArrayList<>();
        int idx = 0;
        while ((idx = content.indexOf("data:image/", idx)) != -1) {
            if (idx == 0) {
                idx += 1;
                continue;
            }
            char quote = content.charAt(idx - 1);
            if (quote != '"' && quote != '\'') {
                idx += 1;
                continue;
            }
            int end = content.indexOf(quote, idx);
            if (end == -1) {
                break;
            }
            uris.add(content.substring(idx, end));
            idx = end;
        }
        return uris;
    }

    // Resolves and downscales every image referenced by the given fields
    // before conversion starts. Downscaling is pure image work with no
    // docx4j involvement, so unlike the conversion itself it can safely run
    // on multiple cores.
    @ProfileMethod("Parallel image fetch and downscale")
    private void warmImageCache(List<String[]> fields) {
        // without downscaling there is nothing to parallelize — the per-field
        // path fetches images with the bounded LRU cache as before, and full
        // size payloads must not pile up in the uncapped warmed map
        if (maxImageWidth <= 0) {
            return;
        }
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "warmImageCache");
        try {
            // collect work: getImage guids not yet cached, and oversized
            // inline data URIs (dedup by hash)
            Set<String> guids = new HashSet<>();
            Map<String, String> inlineByHash = new HashMap<>();
            for (String[] field : fields) {
                String content = field[2];
                if (content.contains("getImage")) {
                    Matcher m = IMAGE_LINK_PATTERN.matcher(content);
                    while (m.find()) {
                        String guid = m.group(2);
                        if (!this.warmedImages.containsKey(guid) && !this.imageCache.containsKey(guid)
                                && !this.imagesNotFound.contains(guid)) {
                            guids.add(guid);
                        }
                    }
                }
                if (content.contains("data:image/")) {
                    for (String uri : extractInlineDataUris(content)) {
                        inlineByHash.put(ReportImageScaler.hash(uri), uri);
                    }
                }
            }

            // fetch raw getImage payloads serially (fast Mongo reads)
            final Map<String, String> rawImages = new HashMap<>();
            if (!guids.isEmpty()) {
                EntityManager em = HibHelper.getInstance().getEM();
                try {
                    for (String guid : guids) {
                        try {
                            Image img = (Image) em.createQuery("SELECT i FROM Image i WHERE i.guid = :guid")
                                    .setParameter("guid", guid)
                                    .getSingleResult();
                            if (img != null && img.getBase64Image() != null) {
                                if (ReportImageScaler.isReportReady(img, maxImageWidth)) {
                                    // rendition prepared at upload — no decode work left
                                    this.warmedImages.put(guid, ReportImageScaler.reportUri(img, maxImageWidth));
                                } else {
                                    rawImages.put(guid, img.getBase64Image());
                                }
                            } else {
                                this.imagesNotFound.add(guid);
                            }
                        } catch (Exception e) {
                            this.imagesNotFound.add(guid);
                        }
                    }
                } finally {
                    em.close();
                }
            }
            if (rawImages.isEmpty() && inlineByHash.isEmpty()) {
                return;
            }

            // downscale everything in parallel
            int threads = Math.min(8, Math.max(2, Runtime.getRuntime().availableProcessors() - 1));
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            final ConcurrentHashMap<String, String> scaledGuids = new ConcurrentHashMap<>();
            try {
                for (final Map.Entry<String, String> entry : rawImages.entrySet()) {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            scaledGuids.put(entry.getKey(),
                                    ReportImageScaler.downscaleDataUri(entry.getValue(), maxImageWidth));
                        }
                    });
                }
                for (final Map.Entry<String, String> entry : inlineByHash.entrySet()) {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            String downscaled = ReportImageScaler.downscaleDataUri(entry.getValue(), maxImageWidth);
                            if (!downscaled.equals(entry.getValue())) {
                                inlineImagesDownscaled.put(entry.getKey(), downscaled);
                            }
                        }
                    });
                }
            } finally {
                pool.shutdown();
                try {
                    pool.awaitTermination(30, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // warmed results live in the per-report map; running them through
            // the LRU cache evicted them before they were ever used
            this.warmedImages.putAll(scaledGuids);
        } finally {
            context.end();
        }
    }

    private void cacheImage(String guid, String base64) {
        if (base64.length() > MAX_IMAGE_CACHE_CHARS / 4) {
            // oversized images are served for the current field only, never
            // held in the cache
            return;
        }
        this.imageCache.put(guid, base64);
        this.imageCacheChars += base64.length();
        java.util.Iterator<Map.Entry<String, String>> eldest = this.imageCache.entrySet().iterator();
        // evict least-recently-used entries until under the cap; keep at
        // least the entry just added so oversized single images still work
        while (this.imageCacheChars > MAX_IMAGE_CACHE_CHARS && this.imageCache.size() > 1 && eldest.hasNext()) {
            Map.Entry<String, String> entry = eldest.next();
            this.imageCacheChars -= entry.getValue().length();
            eldest.remove();
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
    private void insertPageBreaks() {
        // find every ${pageBreak} tag in one pass over the top-level content
        // instead of re-traversing the whole document after each insertion
        List<Object> content = mlp.getMainDocumentPart().getContent();
        for (int i = 0; i < content.size(); i++) {
            Object element = XmlUtils.unwrap(content.get(i));
            if (!(element instanceof P)) {
                continue;
            }
            final StringWriter paragraphText = new StringWriter();
            try {
                TextUtils.extractText(element, paragraphText);
            } catch (Exception ex) {
                continue;
            }
            if (paragraphText.toString().contains("${pageBreak}")) {
                System.out.println("Adding PageBreak");
                content.remove(i);
                addPageBreak(mlp, i);
            }
        }

    }
    
    private void removeSectionTagsOrRemoveSection(String sectionName, Boolean hasVulns) {
        try {
			int begin = -1;
			int end = -1;

			if (Strings.isNullOrEmpty(sectionName)) {
				begin = getIndex(mlp.getMainDocumentPart(), "${if-section}");
				end = getIndex(mlp.getMainDocumentPart(), "${end-section}");
			} else if (sectionName != null && sectionName.equals("Default")) {
				begin = getIndex(mlp.getMainDocumentPart(), "${if-section}");
				end = getIndex(mlp.getMainDocumentPart(), "${end-section}");
			} else if (sectionName != null) {
				begin = getIndex(mlp.getMainDocumentPart(), "${if-section " + sectionName + "}");
				end = getIndex(mlp.getMainDocumentPart(), "${end-section " + sectionName + "}");
			}
			if (begin == -1)
				return;
			if (end == -1)
				return;
			
			// remove only the tags
        	if(hasVulns) {
        			/// DOn't need this because getIndex deletes the element at that index. 
        			//remove in reverse order so it does not break the index and remove the wrong element
					//mlp.getMainDocumentPart().getContent().remove(end);
					//mlp.getMainDocumentPart().getContent().remove(begin);
			// remove everything between the tags
        	}else {
				for (int i = end; i >= begin; i--) {
					mlp.getMainDocumentPart().getContent().remove(i);
				}
        	}
        }catch(Exception ex) {
        	ex.printStackTrace();
        }
    }

    @ProfileMethod("Process findings section and populate with vulnerability data")
    private void setFindings(String section, String customCSS)
            throws JAXBException, Docx4JException {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "setFindings");
        List<Vulnerability> filteredVulns = this.getFilteredVulns(section);
        Boolean hasVulns = !filteredVulns.isEmpty();
        this.removeSectionTagsOrRemoveSection(section, hasVulns);
        
        if(!hasVulns) {
        	return;
        }
        
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
            int count = 1;
            // HTML placeholders get a per-vulnerability suffix (e.g. ${rec:3})
            // so one replaceHTML pass over the document can fill in every
            // finding, instead of a full document traversal per vulnerability.
            // Field content is collected first and converted in batches —
            // each XHTML import pass has a large fixed cost, so one pass per
            // field was the dominant cost of report generation.
            List<String[]> fieldSpecs = new ArrayList<>();
            // cache hits go directly here, bypassing convertFieldsBatched
            HashMap<String, List<Object>> detailsMapCache = new HashMap<>();
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
                        nxml = StringUtils.replace(nxml, "${rec}", "${rec:" + count + "}");
                        nxml = StringUtils.replace(nxml, "${desc}", "${desc:" + count + "}");
                        nxml = StringUtils.replace(nxml, "${details}", "${details:" + count + "}");
                        if (v.getCustomFields() != null) {
                            for (CustomField cf : v.getCustomFields()) {
                                if (cf.getType().getFieldType() == 3) {
                                    nxml = StringUtils.replace(nxml, "${cf" + cf.getType().getVariable() + "}",
                                            "${cf" + cf.getType().getVariable() + ":" + count + "}");
                                }
                            }
                        }
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

                // Add the vulnerability description and recommandations.
                // update custom fields inside the html before inserting into the document.
                // Try the pre-compiled cache first — a hit bypasses the
                // expensive XHTMLImporterImpl.convert() entirely; misses
                // fall through to the batched conversion below.
                List<Object> recCached = wrapHTMLFromCache(v, "rec", count, customCSS);
                if (recCached != null) {
                    detailsMapCache.put("${rec:" + count + "}", recCached);
                } else {
                    fieldSpecs.add(new String[] { "${rec:" + count + "}", "rec",
                            this.replaceFigureVariables(getRecommendation(v), count) });
                }
                List<Object> descCached = wrapHTMLFromCache(v, "desc", count, customCSS);
                if (descCached != null) {
                    detailsMapCache.put("${desc:" + count + "}", descCached);
                } else {
                    fieldSpecs.add(new String[] { "${desc:" + count + "}", "desc",
                            this.replaceFigureVariables(getDescription(v), count) });
                }
                List<Object> detCached = wrapHTMLFromCache(v, "details", count, customCSS);
                if (detCached != null) {
                    detailsMapCache.put("${details:" + count + "}", detCached);
                } else {
                    fieldSpecs.add(new String[] { "${details:" + count + "}", "details",
                            this.replaceFigureVariables(getDetails(v), count) });
                }

                if (v.getCustomFields() != null) {
                    for (CustomField cf : v.getCustomFields()) {
                        if (cf.getType().getFieldType() == 3) {
                            String cfKey = "${cf" + cf.getType().getVariable() + ":" + count + "}";
                            List<Object> cfCached = wrapHTMLFromCache(v,
                                    "cf:" + cf.getType().getVariable(), count, customCSS);
                            if (cfCached != null) {
                                detailsMapCache.put(cfKey, cfCached);
                            } else {
                                fieldSpecs.add(new String[] { cfKey,
                                        cf.getType().getVariable(), cf.getValue() == null ? "" : cf.getValue() });
                            }
                        }
                    }
                }
                count++;

            }
            // merge cache hits with batch-converted misses
            HashMap<String, List<Object>> detailsMap = convertFieldsBatched(fieldSpecs, customCSS);
            detailsMap.putAll(detailsMapCache);
            replaceHTML(mlp.getMainDocumentPart(), detailsMap, true);
        } finally {
            context.end();
        }

    }

    // hot path: recursive and called for every node — intentionally not profiled
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

			// normalize every img tag (self-closed or not) to <img ...></img>;
			// content is no longer pre-tidied, so plain HTML <img ...> tags
			// must be handled too
			content = content.replaceAll("(<img\\b[^>]*?)\\s*/?>(?!</img>)", "$1></img>");
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

			// collect the matched paragraphs first, grouped by their parent
			// content list, so each parent list is rewritten in one pass —
			// an indexOf scan per match is quadratic on large documents
			final Map<List<Object>, Map<Object, List<Object>>> matchesByParent = new IdentityHashMap<>();

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
						Map<Object, List<Object>> matches = matchesByParent.get(listToModify);
						if (matches == null) {
							matches = new IdentityHashMap<>();
							matchesByParent.put(listToModify, matches);
						}
						matches.put(paragraph, replacements.get(identifier));
						if (once)
							replacements.remove(identifier);

					}
				}

			}

			for (Map.Entry<List<Object>, Map<Object, List<Object>>> entry : matchesByParent.entrySet()) {
				final Map<Object, List<Object>> matches = entry.getValue();
				final ListIterator<Object> iterator = entry.getKey().listIterator();
				while (iterator.hasNext() && !matches.isEmpty()) {
					final List<Object> replacement = matches.remove(iterator.next());
					if (replacement != null) {
						// swap the placeholder paragraph for the converted HTML
						iterator.remove();
						for (Object node : replacement) {
							iterator.add(node);
						}
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
    }

    private void updateDocWithExtensions(String customCSS) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "updateDocWithExtensions");
        try {

			// nothing to do without a report extension — skip the traversal
			if (!this.reportExtension.isExtended()) {
				return;
			}

			// force-initialize lazy collections on the assessment that
			// Extensions.cloneChecklists() will access. Before the cache
			// optimization, replacement() called updateReport() for every
			// field, which initialized these collections early. Now that
			// we skip those calls, the collections are never initialized.
			// Re-attach the assessment to a fresh session and touch the
			// lazy collection so it's loaded before cloneChecklists runs.
			try {
				EntityManager em = HibHelper.getInstance().getEM();
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				Assessment managed = em.merge(this.assessment);
				if (managed.getAnswers() != null) {
					managed.getAnswers().size();
				}
				HibHelper.getInstance().commit();
				// copy the initialized collection back to our detached object
				this.assessment = managed;
			} catch (Exception ex) {
				// not critical — extension may not need checklists
			}

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
				// skip the expensive updateReport() call (which clones the
				// entire assessment + all vulns) when this paragraph clearly
				// has no ${ placeholder that an extension could handle.
				// Only paragraphs containing ${faction-bar-chart} or similar
				// extension placeholders will trigger the clone + extension.
				if (!identifier.contains("${")) {
					continue;
				}
				String html = this.reportExtension.updateReport(this.assessment, identifier);
				if (html != null && !html.equals(identifier)) {
					// only pay for the index lookup when the extension
					// actually changed this paragraph
					final List<Object> listToModify = this.getParentElement(paragraph);

					if (listToModify != null) {
						final int index = listToModify.indexOf(paragraph);
						if (index > -1) {
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

    // hot path: called per paragraph — intentionally not profiled
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
        for (Object paragraph : paragraphs) {
            String text = getMatchingText((P) paragraph, variable);
            if (text != null)
                return text;
        }
        return null;
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
		SimpleDateFormat formatter = DATE_FORMAT.get();
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