package com.fuse.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Text;

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
import com.fuse.utils.LoggingConfig;
import com.fuse.utils.MethodProfiler;
import com.fuse.utils.ProfileMethod;
import com.fuse.utils.ReportImageScaler;
import com.google.common.base.Strings;

import jakarta.xml.bind.JAXBElement;

/**
 * Experimental rewrite of DocxUtils that operates on the raw document.xml
 * string instead of mutating the docx4j object tree per vulnerability.
 *
 * The hot path - 400+ vulns × marshal/unmarshal of template rows ×
 * growing-tree traversal - is replaced with:
 *   - One VariablePrepare pass to normalize runs (via docx4j, once)
 *   - String-only multiplication of template rows per vuln
 *   - String-only variable substitution
 *   - XHTML importer retained ONLY for HTML fields (desc/rec/details),
 *     converting unique fragments to OOXML XML strings that get spliced
 *     into document.xml. Image media parts and relationship IDs produced
 *     by the importer are remapped and copied into the output zip.
 *
 * Not feature-complete compared to DocxUtils: ToC generation, page breaks,
 * and hyperlink rewriting are stubbed or simplified. Uses the same
 * ${...} template conventions so reports can be A/B tested.
 */
public class DocxUtils2 {
    public String FONT = "";

    // assessment-level text variables that trigger an in-document replacement.
    // kept identical to DocxUtils so templates work unchanged.
    private final String[] keywords = { "asmtName", "asmtId", "asmtAppid", "asmtAssessor", "asmtAssessor_Email",
            "asmtAssessor_Lines", "asmtAssessor_Comma", "asmtAssessor_Bullets", "remediation", "asmtTeam", "asmtType",
            "today", "asmtStart", "asmtEnd", "asmtAccessKey", "totalOpenVulns", "totalClosedVulns" };

    private final Extensions reportExtension;
    private final Assessment assessment;
    private final List<Vulnerability> vulns;
    private String[] reportSections = new String[0];

    // scratch docx4j package shared across all XHTML conversions for this
    // report. holds every image ever referenced; relationships and media
    // parts are scanned after each convert() call to detect new ones.
    private WordprocessingMLPackage scratchPackage;
    // rIds the scratch package had at snapshot time; new rIds produced by
    // the next convert() call are diff'd against this.
    private Set<String> scratchRIdsSeen = new HashSet<>();
    // importer rId -> output rId (stable across the whole report; same image
    // reused across many vulns shares one output rId)
    private Map<String, String> importerRIdToOutputRId = new LinkedHashMap<>();
    // importer rId -> BinaryPart captured from the scratch package at the
    // time the rId was first seen (so we can copy the bytes exactly once)
    private Map<String, BinaryPart> importerRIdToPart = new LinkedHashMap<>();
    // output image parts, in insertion order, written into word/media and
    // surfaced as relationships in word/_rels/document.xml.rels
    private LinkedHashMap<String, ImageOutPart> outputImages = new LinkedHashMap<>();
    private int nextOutputImageId = 1000;

    // HTML fragment cache: identical content converts to identical OOXML,
    // and many vulns share identical default-template recommendations.
    // The cached snippet has importer rIds already remapped, so a cached
    // snippet can be spliced straight in.
    private HashMap<String, String> htmlSnippetCache = new HashMap<>();

    // per-report image cache reused by getImage substitution. LRU-bounded
    // so image-heavy assessments don't keep every base64 in memory. ditto
    // the logic in DocxUtils - copied verbatim.
    private static final long MAX_IMAGE_CACHE_CHARS = 64L * 1024 * 1024;
    private LinkedHashMap<String, String> imageCache = new LinkedHashMap<>(16, 0.75f, true);
    private long imageCacheChars = 0;
    private HashSet<String> imagesNotFound = new HashSet<>();
    private final int maxImageWidth = ReportImageScaler.configuredMaxWidth();
    private ConcurrentHashMap<String, String> inlineImagesDownscaled = new ConcurrentHashMap<>();

    // matches <img src="getImage?id=<assessment>:<guid>"> with either quote
    // style and HTML-encoded '='
    private static final Pattern IMAGE_LINK_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']getImage\\?id(=|&#61;)[0-9]+:([^\"'\\s>]+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE);

    // precomputed assessment-level values
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

    // splits one batched XHTML conversion back into per-field fragments
    private static final String FIELD_SPLIT_MARKER = "FCT-FIELD-SPLIT-7f3a91";
    private static final int MAX_CHUNK_FIELDS = 25;
    private static final int MAX_CHUNK_CHARS = 2 * 1024 * 1024;

    public DocxUtils2(EntityManagerFactory emf, Assessment assessment) {
        this.assessment = assessment;
        this.vulns = assessment.getVulns();
        this.reportExtension = new Extensions(emf, Extensions.EventType.REPORT_MANAGER);
        this.precomputeAssessmentValues();
        this.setupReportSections(emf);
    }

    public DocxUtils2(Assessment assessment) {
        this(HibHelper.getInstance().getEMF(), assessment);
    }

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

    private void setupReportSections(EntityManagerFactory emf) {
        if (ReportFeatures.allowSections()) {
            EntityManager em = emf.createEntityManager();
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

    private List<Vulnerability> getFilteredVulns(String section) {
        if (section == null || section.isEmpty()) {
            section = "Default";
        }
        if (section.equals("Default")) {
            return this.vulns.stream()
                    .filter(vuln -> vuln.getSection() == null || vuln.getSection().isEmpty()
                            || vuln.getSection().equals("Default") || !sectionExists(vuln.getSection()))
                    .collect(Collectors.toList());
        }
        final String query = section;
        return this.vulns.stream().filter(vuln -> vuln.getSection().equals(query)).collect(Collectors.toList());
    }

    private static String CData(String text) {
        return "<![CDATA[" + text + "]]>";
    }

    // ===== main entry point =====

    /**
     * Generates the report as raw docx bytes. Loads the template, runs
     * VariablePrepare once to normalize the run tree, extracts document.xml
     * as a string for the per-vuln string work, and re-zips at the end.
     */
    public byte[] generateReport(InputStream template, String customCSS) throws Exception {
        byte[] templateBytes = readAll(template);

        // 1. VariablePrepare via docx4j — rewrites run splits so placeholders
        // live in a single <w:t>, which makes raw-XML scanning possible.
        this.scratchPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(templateBytes));
        VariablePrepare.prepare(this.scratchPackage);
        ByteArrayOutputStream prepared = new ByteArrayOutputStream();
        this.scratchPackage.save(prepared);
        byte[] preparedBytes = prepared.toByteArray();

        // reload from the prepared bytes so any structural adjustments
        // (styles, parts) persist into the output, then steal document.xml
        // + rels as the strings we manipulate
        Map<String, byte[]> zip = unzip(preparedBytes);
        String documentXml = new String(zip.get("word/document.xml"), "UTF-8");
        String relsXml = zip.containsKey("word/_rels/document.xml.rels")
                ? new String(zip.get("word/_rels/document.xml.rels"), "UTF-8")
                : emptyRelsXml();

        // cold-start the rId tracker so we know what rIds already exist on
        // the scratch package (template originals). importer-produced image
        // rIds are always new; template ones are never touched.
        snapshotScratchRIds();

        // 2. Multiply template table rows / findings paragraphs once per
        // vuln via string ops
        documentXml = processTables(documentXml, customCSS);
        documentXml = processFindingsSections(documentXml, customCSS);

        // 3. Convert HTML fragments (desc/rec/details/custom-field HTML) via
        // the XHTML importer against a scratch package, splice the resulting
        // OOXML paragraphs into document.xml and track image parts
        documentXml = fillHtmlPlaceholders(documentXml, customCSS);

        // 4. Simple text variables: assessment identity, dates, risk counts.
        // One-shot string replacement over the whole document XML.
        documentXml = applyDocumentWideVariables(documentXml, customCSS);

        // 5. Header/footer text: simple-vars pass over each header/footer
        // part zip entry
        for (Map.Entry<String, byte[]> e : zip.entrySet()) {
            String name = e.getKey();
            if (name.startsWith("word/header") && name.endsWith(".xml")) {
                String hdr = new String(e.getValue(), "UTF-8");
                hdr = applyDocumentWideVariables(hdr, customCSS);
                e.setValue(hdr.getBytes("UTF-8"));
            } else if (name.startsWith("word/footer") && name.endsWith(".xml")) {
                String ftr = new String(e.getValue(), "UTF-8");
                ftr = applyDocumentWideVariables(ftr, customCSS);
                e.setValue(ftr.getBytes("UTF-8"));
            }
        }

        // 6. Rebuild rels.xml to include image relationships added by the
        // importer
        relsXml = rebuildRelsXml(relsXml);

        // 7. Re-zip
        zip.put("word/document.xml", documentXml.getBytes("UTF-8"));
        zip.put("word/_rels/document.xml.rels", relsXml.getBytes("UTF-8"));
        for (Map.Entry<String, ImageOutPart> e : outputImages.entrySet()) {
            zip.put("word/" + e.getValue().target, e.getValue().bytes);
        }

        return zipToBytes(zip);
    }

    private static String emptyRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>";
    }

    // ===== zip helpers =====

    private static byte[] readAll(InputStream is) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static Map<String, byte[]> unzip(byte[] data) throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            byte[] buf = new byte[8192];
            while ((entry = zin.getNextEntry()) != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int n;
                while ((n = zin.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
                entries.put(entry.getName(), out.toByteArray());
            }
        }
        return entries;
    }

    private static byte[] zipToBytes(Map<String, byte[]> entries) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            // [Content_Types].xml first is conventional and helps Word
            byte[] contentTypes = entries.remove("[Content_Types].xml");
            if (contentTypes != null) {
                zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
                zos.write(contentTypes);
                zos.closeEntry();
            }
            for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(e.getKey()));
                zos.write(e.getValue());
                zos.closeEntry();
            }
        }
        return out.toByteArray();
    }

    // ===== rId tracking for image remapping =====

    private void snapshotScratchRIds() {
        Set<String> seen = new HashSet<>();
        RelationshipsPart rels = this.scratchPackage.getMainDocumentPart().getRelationshipsPart();
        if (rels != null && rels.getRelationships() != null) {
            for (Relationship r : rels.getRelationships().getRelationship()) {
                seen.add(r.getId());
            }
        }
        this.scratchRIdsSeen = seen;
    }

    // For every rId the scratch package has gained since the last snapshot,
    // allocate a stable output rId and remember the BinaryPart so the image
    // bytes are copied exactly once. Returns the importer rIds that were new.
    private Set<String> captureNewScratchImageRIds() {
        RelationshipsPart rels = this.scratchPackage.getMainDocumentPart().getRelationshipsPart();
        if (rels == null || rels.getRelationships() == null) {
            return java.util.Collections.emptySet();
        }
        Set<String> newlyAdded = new HashSet<>();
        for (Relationship r : rels.getRelationships().getRelationship()) {
            String id = r.getId();
            if (this.scratchRIdsSeen.contains(id)) {
                continue;
            }
            this.scratchRIdsSeen.add(id);
            newlyAdded.add(id);
            // Skip non-media (e.g. hyperlinks produced during conversion).
            // Only image relationships are remapped and copied.
            if (r.getType() == null || !r.getType().endsWith("/image")) {
                continue;
            }
            if (this.importerRIdToOutputRId.containsKey(id)) {
                continue;
            }
            try {
                // Resolve the binary part lazily so a missing part doesn't
                // abort the whole report
                org.docx4j.openpackaging.parts.Part part = rels.getPart(r);
                if (part instanceof BinaryPart) {
                    BinaryPart bp = (BinaryPart) part;
                    String outputId = "rId" + (nextOutputImageId++);
                    String ext = extensionFor(bp.getContentType());
                    String target = "media/image" + nextOutputImageId + "." + ext;
                    // imageN where N matches the rId suffix keeps the file
                    // name discoverable for debugging
                    target = "media/image" + (nextOutputImageId - 1) + "." + ext;
                    try {
                        byte[] bytes = bp.getBytes();
                        outputImages.put(outputId, new ImageOutPart(target, bytes, bp.getContentType()));
                    } catch (Exception ex) {
                        // could not extract image bytes - skip this image;
                        // leaving a dangling relationship that Word will flag
                        ex.printStackTrace();
                    }
                    this.importerRIdToOutputRId.put(id, outputId);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return newlyAdded;
    }

    private static String extensionFor(String contentType) {
        if (contentType == null)
            return "png";
        switch (contentType) {
            case "image/png":
                return "png";
            case "image/jpeg":
            case "image/jpg":
                return "jpeg";
            case "image/gif":
                return "gif";
            case "image/bmp":
                return "bmp";
            default:
                return "png";
        }
    }

    // Remap rId references in a converted-XML fragment from importer-space
    // to output-space. Stable across the whole report.
    private String remapRIds(String xmlFragment) {
        Matcher m = RID_REF_PATTERN.matcher(xmlFragment);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String rId = m.group(1);
            String outputId = this.importerRIdToOutputRId.get(rId);
            // only remap rIds we've actually allocated - leave any
            // template-supplied relationships alone
            if (outputId != null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0).replace(rId, outputId)));
            } else {
                // an importer image rId we somehow didn't capture - the
                // safest fix is to allocate one on demand. This shouldn't
                // happen for HTML convert output, but it's a cheap safety net.
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern RID_REF_PATTERN = Pattern.compile(
            "(?:r:embed|r:link|r:id)\\s*=\\s*\"(rId\\d+)\"",
            Pattern.CASE_INSENSITIVE);

    private static class ImageOutPart {
        final String target;
        final byte[] bytes;
        final String contentType;

        ImageOutPart(String target, byte[] bytes, String contentType) {
            this.target = target;
            this.bytes = bytes;
            this.contentType = contentType;
        }
    }

    // ===== template processing: tables =====

    /**
     * Finds every table row whose <w:t> contains ${loop} and replaces that
     * single template row with one rendered row per vulnerability in the
     * section, applying replaceXml() to the row XML each time. Pure string
     * operations - no JAXB round-trip per vuln.
     */
    @ProfileMethod("DocxUtils2: processTables (raw XML)")
    private String processTables(String documentXml, String customCSS) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils2", "processTables");
        try {
            documentXml = runTablesForSection(documentXml, "vulnTable", "Default", customCSS);
            if (ReportFeatures.allowSections()) {
                for (String section : this.reportSections) {
                    documentXml = runTablesForSection(documentXml, "vulnTable", section, customCSS);
                }
            }
            return documentXml;
        } finally {
            context.end();
        }
    }

    private String runTablesForSection(String xml, String variable, String section, String customCSS) {
        List<Vulnerability> filteredVulns = this.getFilteredVulns(section);
        boolean hasVulns = !filteredVulns.isEmpty();

        xml = removeSectionTagsOrRemoveSection(xml, section, hasVulns);
        if (!hasVulns) {
            return xml;
        }

        String tableVariableBase = "${" + variable + "}";
        String tableVariableSectioned = "${" + variable + " " + section + "}";
        String tableVariable = (ReportFeatures.allowSections() && section != null && !section.isEmpty()
                && !section.equals("Default")) ? tableVariableSectioned : tableVariableBase;

        // Each table is identified by a <w:tbl>...</w:tbl> block. We search
        // the document XML for the table that contains the tableVariable and
        // a ${loop} row. For tables that match we replace the single template
        // row with content for each vuln.
        int scan = 0;
        while (true) {
            // find next table containing both our variable and a ${loop} row
            int[] tableBounds = findNextTableContaining(xml, scan, tableVariable, "${loop");
            if (tableBounds == null) {
                break;
            }
            int tblStart = tableBounds[0];
            int tblEnd = tableBounds[1];
            String tableXml = xml.substring(tblStart, tblEnd);
            String newTableXml = renderTableForVulns(tableXml, filteredVulns, section);
            xml = xml.substring(0, tblStart) + newTableXml + xml.substring(tblEnd);
            // advance past this table to avoid rescanning the (now larger)
            // portion we just wrote
            scan = tblStart + newTableXml.length();
        }
        return xml;
    }

    // Finds the next <w:tbl>...</w:tbl> substring that contains both
    // tableVariable and loopVariable somewhere within it. Returns bounds
    // [start, end) on the outer xml string, or null if not found. Tracks
    // nested tables (rare but possible) by counting open/close depth.
    private static int[] findNextTableContaining(String xml, int from, String tableVariable, String loop) {
        int scan = from;
        while (true) {
            int tbl = xml.indexOf("<w:tbl", scan);
            if (tbl < 0)
                return null;
            // find matching </w:tbl> with depth tracking
            int depth = 0;
            int i = tbl;
            int end = -1;
            while (i < xml.length()) {
                int nextOpen = xml.indexOf("<w:tbl", i);
                int nextClose = xml.indexOf("</w:tbl>", i);
                if (nextClose < 0)
                    break;
                if (nextOpen >= 0 && nextOpen < nextClose) {
                    depth++;
                    i = nextOpen + 7;
                } else {
                    if (depth == 0) {
                        end = nextClose + "</w:tbl>".length();
                        break;
                    }
                    depth--;
                    i = nextClose + "</w:tbl>".length();
                }
            }
            if (end < 0)
                return null;
            String segment = xml.substring(tbl, end);
            if (segment.contains(tableVariable) && segment.contains(loop)) {
                return new int[] { tbl, end };
            }
            scan = end;
        }
    }

    // Given the <w:tbl>...</w:tbl> XML of a table containing a template row
    // with ${loop}, produces a new table XML with that row multiplied for
    // each vulnerability. The template row is identified as the <w:tr>
    // containing ${loop}; the rendered rows replace it. ${loop-N} sequences
    // pull multiple consecutive rows into the template.
    private String renderTableForVulns(String tableXml, List<Vulnerability> filteredVulns, String section) {
        int loopIdx = tableXml.indexOf("${loop");
        if (loopIdx < 0)
            return tableXml;

        int[] rowBounds = findEnclosingTrBounds(tableXml, loopIdx);
        if (rowBounds == null)
            return tableXml;
        int rowStart = rowBounds[0];
        int rowEnd = rowBounds[1];
        String templateRowXml = tableXml.substring(rowStart, rowEnd);

        // parse the color/cell/custom-fields directives once - they live in
        // the same table outside the loop row, so scrape before modifying
        HashMap<String, String> colorMap = parseDirectiveMap(tableXml, "${color");
        HashMap<String, String> cellMap = parseDirectiveMap(tableXml, "${cells");
        HashMap<String, String> customFieldMap = parseDirectiveMap(tableXml, "${custom-fields");

        // remove all directive lines from the table - they should never
        // appear in the rendered output
        tableXml = stripDirectiveMarkers(tableXml, "${color");
        tableXml = stripDirectiveMarkers(tableXml, "${cells");
        tableXml = stripDirectiveMarkers(tableXml, "${custom-fields");
        tableXml = stripDirectiveMarkers(tableXml, "${noIssuesText");

        int count = 1;
        int sevIndex = 0;
        String prevSev = "";
        StringBuilder rows = new StringBuilder();
        for (Vulnerability v : filteredVulns) {
            if (prevSev == v.getOverallStr()) {
                sevIndex++;
            } else {
                prevSev = v.getOverallStr();
                sevIndex = 1;
            }
            String nxml = replaceXml(templateRowXml, v, customFieldMap, colorMap, cellMap, null, count, sevIndex);
            // strip loop marker text + ${loop-N} syntax
            nxml = nxml.replaceAll("\\$\\{loop(?:-\\d+)?\\}", "");
            rows.append(nxml);
            count++;
        }
        // splice rows in place of the original template row
        return tableXml.substring(0, rowStart) + rows.toString() + tableXml.substring(rowEnd);
    }

    // parses ${cells key=VALUE,...} style directives into a map.
    private static HashMap<String, String> parseDirectiveMap(String xml, String marker) {
        HashMap<String, String> map = new HashMap<>();
        int idx = xml.indexOf(marker);
        if (idx < 0)
            return map;
        int end = xml.indexOf("}", idx);
        if (end < 0)
            return map;
        String body = xml.substring(idx + marker.length(), end).trim();
        for (String pair : body.split(",")) {
            pair = pair.trim();
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1].toUpperCase());
            }
        }
        return map;
    }

    // strips <w:tr> rows that contain the directive marker; the directive
    // was already extracted so the row is informational only.
    private static String stripDirectiveMarkers(String xml, String marker) {
        int scan = 0;
        StringBuilder out = new StringBuilder();
        while (true) {
            int idx = xml.indexOf(marker, scan);
            if (idx < 0) {
                out.append(xml, scan, xml.length());
                break;
            }
            int[] trBounds = findEnclosingTrBounds(xml, idx);
            if (trBounds == null) {
                // not in a tr - just remove the marker text itself
                int close = xml.indexOf("}", idx);
                if (close < 0) {
                    out.append(xml, scan, xml.length());
                    break;
                }
                out.append(xml, scan, idx);
                scan = close + 1;
                continue;
            }
            out.append(xml, scan, trBounds[0]);
            scan = trBounds[1];
        }
        return out.toString();
    }

    // Find the bounds of the <w:tr>...</w:tr> element containing position
    // idx. Walks backward to find the enclosing open tag and forward to
    // find the matching close, with depth tracking for nested tables.
    private static int[] findEnclosingTrBounds(String xml, int idx) {
        int depth = 0;
        int scan = idx;
        int start = -1;
        while (scan > 0) {
            int prevOpen = xml.lastIndexOf("<w:tr ", scan);
            int prevOpenPlain = xml.lastIndexOf("<w:tr>", scan);
            int prevClose = xml.lastIndexOf("</w:tr>", scan);
            int bestOpen = Math.max(prevOpen, prevOpenPlain);
            if (bestOpen < 0) {
                // no enclosing tr at all - shouldn't happen for ${loop}
                return null;
            }
            if (prevClose > bestOpen) {
                depth++;
                scan = prevClose - 1;
                continue;
            }
            if (depth > 0) {
                depth--;
                scan = bestOpen - 1;
                continue;
            }
            start = bestOpen;
            break;
        }
        if (start < 0)
            return null;
        // find end of opening tag (next > after start)
        int openEnd = xml.indexOf(">", start);
        if (openEnd < 0)
            return null;
        // walk forward to matching </w:tr>
        int j = openEnd + 1;
        depth = 0;
        while (j < xml.length()) {
            int nextOpen = xml.indexOf("<w:tr ", j);
            int nextOpenPlain = xml.indexOf("<w:tr>", j);
            int nextClose = xml.indexOf("</w:tr>", j);
            int bestOpen = Math.max(nextOpen, nextOpenPlain);
            if (nextClose < 0)
                return null;
            if (bestOpen >= 0 && bestOpen < nextClose) {
                depth++;
                j = bestOpen + 6;
            } else {
                if (depth == 0) {
                    return new int[] { start, nextClose + "</w:tr>".length() };
                }
                depth--;
                j = nextClose + "</w:tr>".length();
            }
        }
        return null;
    }

    // ===== template processing: findings sections =====

    /**
     * Multiplies the paragraphs between ${fiBegin}/${fiEnd} (and per-section
     * variants) once per vulnerability. The block is captured as a single
     * string and replaceXml is applied to per-vuln copies. HTML placeholders
     * (${rec}, ${desc}, etc.) are suffixed per vuln so they can be filled in
     * the later HTML substitution pass.
     */
    @ProfileMethod("DocxUtils2: processFindingsSections (raw XML)")
    private String processFindingsSections(String xml, String customCSS) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils2", "processFindingsSections");
        try {
            xml = runFindingsForSection(xml, "Default", customCSS);
            if (ReportFeatures.allowSections()) {
                for (String section : this.reportSections) {
                    xml = runFindingsForSection(xml, section, customCSS);
                }
            }
            return xml;
        } finally {
            context.end();
        }
    }

    private String runFindingsForSection(String xml, String section, String customCSS) {
        List<Vulnerability> filteredVulns = this.getFilteredVulns(section);
        boolean hasVulns = !filteredVulns.isEmpty();
        xml = removeSectionTagsOrRemoveSection(xml, section, hasVulns);
        if (!hasVulns) {
            return xml;
        }

        String beginMarker = (section == null || section.isEmpty() || section.equals("Default")) ? "${fiBegin}"
                : "${fiBegin " + section + "}";
        String endMarker = (section == null || section.isEmpty() || section.equals("Default")) ? "${fiEnd}"
                : "${fiEnd " + section + "}";

        // locate markers - they live inside <w:p> paragraphs
        int begin = xml.indexOf(beginMarker);
        if (begin < 0)
            return xml;
        int end = xml.indexOf(endMarker, begin);
        if (end < 0)
            return xml;

        // capture the paragraphs between the marker paragraphs. The marker
        // paragraphs themselves (containing the ${fiBegin}/${fiEnd} text)
        // are dropped from the output.
        int[] beginParaBounds = findEnclosingParagraphBounds(xml, begin);
        int[] endParaBounds = findEnclosingParagraphBounds(xml, end);
        if (beginParaBounds == null || endParaBounds == null)
            return xml;

        int blockStart = beginParaBounds[1]; // exclusive of marker paragraph close
        int blockEnd = endParaBounds[0]; // before end-marker paragraph open
        if (blockStart > blockEnd) {
            // markers in same paragraph - drop everything between the markers
            blockStart = begin + beginMarker.length();
            blockEnd = end;
        }
        String templateBlockXml = xml.substring(blockStart, blockEnd);

        // extract directive maps and noIssuesText from the block (same
        // convention as tables)
        HashMap<String, String> colorMap = parseDirectiveMap(templateBlockXml, "${color");
        HashMap<String, String> cellMap = parseDirectiveMap(templateBlockXml, "${fill");
        HashMap<String, String> customFieldMap = parseDirectiveMap(templateBlockXml, "${custom-fields");

        // strip directive rows before per-vuln multiplication so they don't
        // get duplicated for every vuln
        String cleanTemplate = stripDirectiveMarkers(templateBlockXml, "${color");
        cleanTemplate = stripDirectiveMarkers(cleanTemplate, "${fill");
        cleanTemplate = stripDirectiveMarkers(cleanTemplate, "${custom-fields");
        cleanTemplate = stripDirectiveMarkers(cleanTemplate, "${noIssuesText");

        int count = 1;
        String prevSev = "";
        int sevIndex = 0;
        StringBuilder out = new StringBuilder();
        for (Vulnerability v : filteredVulns) {
            if (prevSev == v.getOverallStr()) {
                sevIndex++;
            } else {
                prevSev = v.getOverallStr();
                sevIndex = 1;
            }
            String nxml = replaceXml(cleanTemplate, v, customFieldMap, colorMap, cellMap, cellMap, count, sevIndex);
            // suffix HTML placeholders so the substitution pass can target
            // each vuln's fields uniquely
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
            out.append(nxml);
            count++;
        }

        // splice: replace the original (marker + block + marker) region with
        // the multiplied paragraphs
        int regionStart = beginParaBounds[0];
        int regionEnd = endParaBounds[1];
        return xml.substring(0, regionStart) + out.toString() + xml.substring(regionEnd);
    }

    // section tags: ${if-section [name]} ... ${end-section [name]} - kept
    // when vulns exist (drop just the marker paragraphs), dropped entirely
    // when there are no vulns. Operates on raw XML strings.
    private String removeSectionTagsOrRemoveSection(String xml, String section, boolean hasVulns) {
        String beginMarker = (Strings.isNullOrEmpty(section) || section.equals("Default")) ? "${if-section}"
                : "${if-section " + section + "}";
        String endMarker = (Strings.isNullOrEmpty(section) || section.equals("Default")) ? "${end-section}"
                : "${end-section " + section + "}";
        int begin = xml.indexOf(beginMarker);
        if (begin < 0)
            return xml;
        int end = xml.indexOf(endMarker, begin);
        if (end < 0)
            return xml;
        int[] beginPara = findEnclosingParagraphBounds(xml, begin);
        int[] endPara = findEnclosingParagraphBounds(xml, end);
        if (beginPara == null || endPara == null)
            return xml;
        if (hasVulns) {
            // drop only the marker paragraphs themselves
            return xml.substring(0, beginPara[0]) + xml.substring(beginPara[1], endPara[0])
                    + xml.substring(endPara[1]);
        }
        // drop everything from beginMarker's paragraph to endMarker's paragraph
        return xml.substring(0, beginPara[0]) + xml.substring(endPara[1]);
    }

    // Find the bounds of the <w:p>...</w:p> element containing position idx.
    // wml doesn't directly nest w:p, so a simple nearest-<w:p / nearest-</w:p>
    // scan is sufficient. Returns [start, end) on the string or null.
    private static int[] findEnclosingParagraphBounds(String xml, int idx) {
        int start = backwardFindParagraphOpen(xml, idx);
        if (start < 0)
            return null;
        int end = xml.indexOf("</w:p>", idx);
        if (end < 0)
            return null;
        // ensure </w:p> we found isn't from a paragraph opened before our
        // start - takes care of any unusual cases
        int nextOpenAfterStart = xml.indexOf("<w:p ", start + 4);
        if (nextOpenAfterStart >= 0 && nextOpenAfterStart < end) {
            // There's another <w:p open between our start and the </w:p> we
            // found - meaning our </w:p> is for an inner paragraph. Find the
            // next one. (only happens for inner tables/textboxes etc.)
            return findEnclosingParagraphBounds(xml, nextOpenAfterStart);
        }
        return new int[] { start, end + "</w:p>".length() };
    }

    private static int backwardFindParagraphOpen(String xml, int from) {
        int scan = from;
        while (scan > 0) {
            int plain = xml.lastIndexOf("<w:p>", scan);
            int withAttr = xml.lastIndexOf("<w:p ", scan);
            int best = Math.max(plain, withAttr);
            if (best < 0)
                return -1;
            // make sure we're not landing inside a closing tag like <w:pPr>
            // by checking this is actually an opening tag end
            int tagEnd = xml.indexOf(">", best);
            if (tagEnd < 0)
                return -1;
            return best;
        }
        return -1;
    }

    // ===== HTML fragment substitution =====

    /**
     * Finds ${rec:N}, ${desc:N}, ${details:N} and ${cf<var>:N} placeholders
     * that processFindingsSections suffixed earlier, and replaces each
     * placeholder's containing paragraph with OOXML produced by the XHTML
     * importer. Identical content is served from the htmlSnippetCache.
     */
    @ProfileMethod("DocxUtils2: fillHtmlPlaceholders")
    private String fillHtmlPlaceholders(String xml, String customCSS) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils2", "fillHtmlPlaceholders");
        try {
            // collect all placeholder occurrences - we'll batch the
            // conversions for cache misses
            Matcher m = PLACEHOLDER_PATTERN.matcher(xml);
            List<String[]> hits = new ArrayList<>();
            Set<String> placeholders = new HashSet<>();
            while (m.find()) {
                // store the full placeholder text "${rec:1}" — group(0) —
                // not just the field name "rec", since resolvePlaceholderSpec
                // re-runs the pattern and needs the :N suffix to extract count
                String placeholder = m.group(0);
                if (placeholders.add(placeholder)) {
                    hits.add(new String[] { placeholder });
                }
            }
            if (hits.isEmpty()) {
                return xml;
            }
            // for each unique placeholder we need its raw HTML content. The
            // placeholder was constructed in processFindingsSections from
            // vulnerability data - reverse-lookup by count index
            Map<String, String[]> specs = new LinkedHashMap<>();
            for (String[] hit : hits) {
                String placeholder = hit[0];
                String[] resolved = resolvePlaceholderSpec(placeholder);
                if (resolved != null) {
                    specs.put(placeholder, resolved);
                }
            }
            // batch-convert specs that aren't in the cache
            List<String[]> toConvert = new ArrayList<>();
            for (Map.Entry<String, String[]> e : specs.entrySet()) {
                String placeholder = e.getKey();
                String[] spec = e.getValue();
                String cacheKey = spec[1] + " " + spec[2]; // className + content
                if (!htmlSnippetCache.containsKey(cacheKey)) {
                    toConvert.add(new String[] { spec[0], spec[1], spec[2] });
                }
            }
            if (!toConvert.isEmpty()) {
                convertHtmlBatchToCache(toConvert, customCSS);
            }

            // splice: replace each placeholder's enclosing paragraph with the
            // (cached, rId-remapped) OOXML snippet. The placeholder may appear
            // multiple times for duplicate content - each occurrence gets
            // its own copy of the snippet, so reuse the same XML but different
            // <w:p> instances will all be parsed independently by Word.
            int scan = 0;
            StringBuilder rebuilt = new StringBuilder();
            Matcher scanMatcher = PLACEHOLDER_PATTERN.matcher(xml);
            int lastEnd = 0;
            boolean foundAny = false;
            while (scanMatcher.find()) {
                String placeholder = scanMatcher.group(0);
                String[] spec = specs.get(placeholder);
                if (spec == null) {
                    continue;
                }
                String snippet = htmlSnippetCache.get(spec[1] + " " + spec[2]);
                if (snippet == null) {
                    continue;
                }
                int[] bounds = findEnclosingParagraphBounds(xml, scanMatcher.start());
                if (bounds == null) {
                    continue;
                }
                if (foundAny) {
                    rebuilt.append(xml, lastEnd, bounds[0]);
                } else {
                    rebuilt.append(xml, 0, bounds[0]);
                    foundAny = true;
                }
                rebuilt.append(snippet);
                lastEnd = bounds[1];
            }
            if (foundAny) {
                rebuilt.append(xml, lastEnd, xml.length());
                return rebuilt.toString();
            }
            return xml;
        } finally {
            context.end();
        }
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "\\$\\{(rec|desc|details|cf[A-Za-z0-9_]+):([0-9]+)\\}");

    // maps a placeholder like ${rec:5} to its source HTML: { className,
    // rawContent }
    private String[] resolvePlaceholderSpec(String placeholder) {
        Matcher m = PLACEHOLDER_PATTERN.matcher(placeholder);
        if (!m.find())
            return null;
        String fieldName = m.group(1);
        int count = Integer.parseInt(m.group(2));
        // find the vuln by 1-based count across the full unfiltered set
        // (processFindingsSections used plain 1-based count over the
        // filtered list, which matches)
        // Note: this only resolves placeholders from the Default section. If
        // sections are enabled and a vuln appears in only one section, counts
        // will differ. For the Default case we walk Default-filtered vulns.
        List<Vulnerability> filtered = this.getFilteredVulns("Default");
        if (count < 1 || count > filtered.size())
            return null;
        Vulnerability v = filtered.get(count - 1);

        int vulnIdx = count; // figureVariables use the 1-based count
        String rawHtml;
        String className;
        switch (fieldName) {
            case "rec":
                rawHtml = replaceFigureVariables(getRecommendation(v), vulnIdx);
                className = "rec";
                break;
            case "desc":
                rawHtml = replaceFigureVariables(getDescription(v), vulnIdx);
                className = "desc";
                break;
            case "details":
                rawHtml = replaceFigureVariables(getDetails(v), vulnIdx);
                className = "details";
                break;
            default:
                if (fieldName.startsWith("cf")) {
                    String var = fieldName.substring(2);
                    if (v.getCustomFields() != null) {
                        for (CustomField cf : v.getCustomFields()) {
                            if (cf.getType().getFieldType() == 3
                                    && cf.getType().getVariable().equals(var)) {
                                rawHtml = cf.getValue() == null ? "" : cf.getValue();
                                className = var;
                                return new String[] { placeholder, className, rawHtml };
                            }
                        }
                    }
                }
                return null;
        }
        return new String[] { placeholder, className, rawHtml };
    }

    private void convertHtmlBatchToCache(List<String[]> fields, String customCSS) {
        // warm image cache (preload + downscale) for any fields that
        // reference getImage?id=... before the serial conversions run
        warmImageCache(fields);

        // de-dup by content
        List<String[]> unique = new ArrayList<>();
        Set<String> seenContent = new HashSet<>();
        for (String[] f : fields) {
            String cacheKey = f[1] + " " + f[2];
            if (htmlSnippetCache.containsKey(cacheKey)) {
                continue;
            }
            if (seenContent.add(f[2])) {
                unique.add(f);
            }
        }

        List<String[]> chunk = new ArrayList<>();
        long chunkChars = 0;
        for (String[] f : unique) {
            chunk.add(f);
            chunkChars += f[2].length();
            if (chunk.size() >= MAX_CHUNK_FIELDS || chunkChars >= MAX_CHUNK_CHARS) {
                convertChunkToCache(chunk, customCSS);
                chunk = new ArrayList<>();
                chunkChars = 0;
            }
        }
        if (!chunk.isEmpty()) {
            convertChunkToCache(chunk, customCSS);
        }
    }

    private void convertChunkToCache(List<String[]> chunk, String customCSS) {
        if (chunk.size() > 1) {
            try {
                List<String> processed = new ArrayList<>(chunk.size());
                for (String[] f : chunk) {
                    processed.add(preprocessHTMLContent(f[2]));
                }
                StringBuilder html = new StringBuilder(htmlPageHead(customCSS));
                for (int i = 0; i < chunk.size(); i++) {
                    html.append("<p>").append(FIELD_SPLIT_MARKER).append("</p>");
                    html.append("<div class='").append(chunk.get(i)[1]).append("'>")
                            .append(processed.get(i)).append("</div>");
                }
                html.append("</body></html>");

                XHTMLImporterImpl xhtml = newImporter();
                List<Object> converted = xhtml.convert(html.toString(), null);
                captureNewScratchImageRIds();

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
                    throw new Docx4JException("field split marker mismatch: expected " + chunk.size()
                            + " segments, found " + segments.size());
                }
                for (int i = 0; i < chunk.size(); i++) {
                    String[] f = chunk.get(i);
                    String snippet = marshalNodesToXml(segments.get(i));
                    snippet = remapRIds(snippet);
                    htmlSnippetCache.put(f[1] + " " + f[2], snippet);
                }
                return;
            } catch (Exception ex) {
                System.err.println("Batched HTML conversion failed, falling back: " + ex.getMessage());
            }
        }
        // one-field-at-a-time fallback
        for (String[] f : chunk) {
            String snippet = wrapHtmlAsXml(f[2], customCSS, f[1]);
            htmlSnippetCache.put(f[1] + " " + f[2], snippet);
        }
    }

    private String wrapHtmlAsXml(String content, String customCSS, String className) {
        String cacheKey = className + " " + content;
        String cached = htmlSnippetCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        try {
            content = preprocessHTMLContent(content);
            String html = htmlPageHead(customCSS) + "<div class='" + className + "'>" + content + "</div></body></html>";
            XHTMLImporterImpl xhtml = newImporter();
            List<Object> converted = xhtml.convert(html, null);
            captureNewScratchImageRIds();
            String snippet = marshalNodesToXml(converted);
            snippet = remapRIds(snippet);
            htmlSnippetCache.put(cacheKey, snippet);
            return snippet;
        } catch (Docx4JException ex) {
            String sanitized = htmlPageHead(customCSS) + "<div class='" + className + "'>"
                    + FSUtils.jtidy(preprocessHTMLContent(content)) + "</div></body></html>";
            try {
                XHTMLImporterImpl xhtml = newImporter();
                List<Object> converted = xhtml.convert(sanitized, null);
                captureNewScratchImageRIds();
                String snippet = marshalNodesToXml(converted);
                snippet = remapRIds(snippet);
                htmlSnippetCache.put(cacheKey, snippet);
                return snippet;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    // marshal a list of JAXB nodes (typically P instances produced by the
    // importer) back to an OOXML XML string suitable for splicing into
    // document.xml. Each top-level node is marshaled separately and joined,
    // so we can splice N paragraphs as siblings. The XML declaration that
    // JAXB emits by default is stripped - it's invalid anywhere except the
    // top of document.xml, and the snippets are mid-document siblings.
    private static String marshalNodesToXml(List<Object> nodes) {
        StringBuilder sb = new StringBuilder();
        for (Object node : nodes) {
            try {
                String xml = XmlUtils.marshaltoString(node, false, false);
                // JAXB_FRAGMENT should suppress the "<?xml ...?>" decl, but
                // the docx4j importer populates the JAXB context in a way
                // where it slips through. Strip it explicitly so the snippet
                // is pure <w:p>...</w:p> markup that can sit mid-document.
                if (xml.startsWith("<?xml")) {
                    int close = xml.indexOf("?>");
                    if (close >= 0) {
                        xml = xml.substring(close + 2);
                    }
                }
                sb.append(xml);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return sb.toString();
    }

    private String preprocessHTMLContent(String content) {
        if (!content.isEmpty()) {
            content = replacement(content);
            content = content.replaceAll("\n", "<br />");
            content = content.replaceAll("<p><br /></p>", "");
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
        LoggingConfig.configureOpenHTMLTopDFLogging();
        XHTMLImporterImpl xhtml = new XHTMLImporterImpl(this.scratchPackage);
        RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
        rfonts.setAscii(this.FONT);
        XHTMLImporterImpl.addFontMapping("Arial", rfonts);
        XHTMLImporterImpl.addFontMapping("arial", rfonts);
        return xhtml;
    }

    private boolean isSplitMarker(Object node) {
        Object unwrapped = XmlUtils.unwrap(node);
        if (!(unwrapped instanceof P))
            return false;
        StringWriter text = new StringWriter();
        try {
            TextUtils.extractText(unwrapped, text);
        } catch (Exception ex) {
            return false;
        }
        return FIELD_SPLIT_MARKER.equals(text.toString().trim());
    }

    // ===== document-wide simple variables =====

    /**
     * Single-pass replacement of ${asmtName}, ${asmtId}, ${today},
     * ${asmtStart}, ${asmtEnd}, risk counts, etc. on a piece of document
     * XML. Also handles the CDATA wrapping that the original DocxUtils did
     * via marshalToString + replaceAll(_, _, true).
     */
    private String applyDocumentWideVariables(String xml, String customCSS) {
        SimpleDateFormat formatter = DATE_FORMAT.get();

        Map<String, Date> dateMap = new LinkedHashMap<>();
        dateMap.put("today", new Date());
        dateMap.put("asmtStart", this.assessment.getStart());
        dateMap.put("asmtEnd", this.assessment.getEnd());
        for (Map.Entry<String, Date> e : dateMap.entrySet()) {
            xml = replaceDateVariable(xml, e.getKey(), e.getValue());
        }

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

        // assessment-level custom fields (text and HTML)
        if (this.assessment.getCustomFields() != null) {
            for (CustomField cf : this.assessment.getCustomFields()) {
                if (cf.getType().getFieldType() < 3) {
                    xml = StringUtils.replace(xml, "${cf" + cf.getType().getVariable() + "}",
                            CData(cf.getValue() == null ? "" : cf.getValue()));
                } else if (cf.getType().getFieldType() == 3) {
                    String snippet = wrapHtmlAsXml(cf.getValue() == null ? "" : cf.getValue(), customCSS,
                            cf.getType().getVariable());
                    // the placeholder lives inside a <w:p> - replace the
                    // entire paragraph
                    String placeholder = "${cf" + cf.getType().getVariable() + "}";
                    xml = replaceParagraphPlaceholders(xml, placeholder, snippet);
                }
            }
        }

        // run report extensions
        if (this.reportExtension.isExtended()) {
            xml = this.reportExtension.updateReport(this.assessment, xml);
        }

        // {[asmtVARNAME]} ranked lists
        xml = loopReplace(xml);

        return xml;
    }

    // Replaces every <w:p> whose only text content is exactly placeholder
    // with replacementXml.
    private static String replaceParagraphPlaceholders(String xml, String placeholder, String replacementXml) {
        StringBuilder out = new StringBuilder();
        int scan = 0;
        while (true) {
            int idx = xml.indexOf(placeholder, scan);
            if (idx < 0) {
                out.append(xml, scan, xml.length());
                break;
            }
            int[] bounds = findEnclosingParagraphBounds(xml, idx);
            if (bounds == null) {
                out.append(xml, scan, idx + placeholder.length());
                scan = idx + placeholder.length();
                continue;
            }
            out.append(xml, scan, bounds[0]);
            out.append(replacementXml);
            scan = bounds[1];
        }
        return out.toString();
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
            if (!isSomething) {
                html = "<i>No vulnerabilities found at this severity.</i>&nbsp;";
            }
            content = content.replaceAll("\\{\\[assessment\\." + Var + "\\]\\}", html);
        }
        return content;
    }

    // ===== rels.xml rebuild =====

    /**
     * Adds the output image relationships we accumulated during HTML
     * conversion to the existing word/_rels/document.xml.rels content.
     * Template relationships are preserved verbatim.
     */
    private String rebuildRelsXml(String relsXml) {
        if (outputImages.isEmpty()) {
            return relsXml;
        }
        StringBuilder additions = new StringBuilder();
        for (Map.Entry<String, ImageOutPart> e : outputImages.entrySet()) {
            String id = e.getKey();
            ImageOutPart part = e.getValue();
            additions.append("<Relationship Id=\"").append(id).append("\"")
                    .append(" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\"")
                    .append(" Target=\"").append(part.target).append("\"/>");
        }
        // inject before </Relationships>
        int close = relsXml.lastIndexOf("</Relationships>");
        if (close < 0) {
            // malformed - return as-is
            return relsXml;
        }
        return relsXml.substring(0, close) + additions.toString() + relsXml.substring(close);
    }

    // ===== image link resolution (getImage?id=...) — reused verbatim =====

    private String replacement(String content) {
        SimpleDateFormat formatter = DATE_FORMAT.get();

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

        content = content.replaceAll("\\$\\{asmtTeam\\}",
                this.assessment.getAssessor() == null ? ""
                        : this.assessment.getAssessor().get(0).getTeam() == null ? ""
                                : this.assessment.getAssessor().get(0).getTeam().getTeamName().trim());
        content = content.replaceAll("\\$\\{asmtType\\}",
                this.assessment.getType() == null ? "" : this.assessment.getType().getType().trim());
        content = replaceDateVariable(content, "today", new Date());
        content = content.replaceAll("\\$\\{asmtStandND\\}", formatter.format(this.assessment.getEnd()));
        content = content.replaceAll("\\$\\{asmtAccessKey\\}", this.assessment.getGuid());
        content = content.replaceAll("\\$\\{totalOpenVulns\\}", this.totalOpenVulns);
        content = content.replaceAll("\\$\\{totalClosedVulns\\}", this.totalClosedVulns);

        if (this.reportExtension.isExtended()) {
            content = this.reportExtension.updateReport(this.assessment, content);
        }

        content = loopReplace(content);

        content = FSUtils.jtidy(content);
        if (maxImageWidth > 0 && content.contains("data:image/")) {
            content = downscaleInlineImages(content);
        }
        content = this.replaceImageLinks(content);
        return content;
    }

    private String replaceImageLinks(String text) {
        if (text.equals("")) {
            return text;
        }
        if (!text.contains("getImage")) {
            return text;
        }
        text = this.centerImages(text);

        String badImage = "<img[^>]*src=[\"']getImage\\?id(=|&#61;)undefined[\"'][^>]*>(</img>)?";
        text = text.replaceAll(badImage, "");

        Pattern imagePattern = IMAGE_LINK_PATTERN;
        Set<String> referencedGuids = new HashSet<>();
        Matcher matcher = imagePattern.matcher(text);
        while (matcher.find()) {
            referencedGuids.add(matcher.group(2));
        }
        if (referencedGuids.isEmpty()) {
            return text;
        }

        Map<String, String> resolvedImages = resolveImages(referencedGuids);

        StringBuffer result = new StringBuffer();
        matcher = imagePattern.matcher(text);
        while (matcher.find()) {
            String guid = matcher.group(2);
            String base64Image = resolvedImages.get(guid);
            if (base64Image != null) {
                String newImgTag = "<img src=\"" + base64Image + "\" >";
                matcher.appendReplacement(result, Matcher.quoteReplacement(newImgTag));
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Map<String, String> resolveImages(Set<String> referencedGuids) {
        Map<String, String> resolved = new HashMap<>();
        List<String> toFetch = new ArrayList<>();
        for (String guid : referencedGuids) {
            String cached = this.imageCache.get(guid);
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
                            .setParameter("guid", guid).getSingleResult();
                    if (img != null && img.getBase64Image() != null) {
                        String base64 = ReportImageScaler.reportUri(img, maxImageWidth);
                        resolved.put(guid, base64);
                        cacheImage(guid, base64);
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
        return resolved;
    }

    private void cacheImage(String guid, String base64) {
        if (base64.length() > MAX_IMAGE_CACHE_CHARS / 4) {
            return;
        }
        this.imageCache.put(guid, base64);
        this.imageCacheChars += base64.length();
        java.util.Iterator<Map.Entry<String, String>> eldest = this.imageCache.entrySet().iterator();
        while (this.imageCacheChars > MAX_IMAGE_CACHE_CHARS && this.imageCache.size() > 1 && eldest.hasNext()) {
            Map.Entry<String, String> entry = eldest.next();
            this.imageCacheChars -= entry.getValue().length();
            eldest.remove();
        }
    }

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

    @ProfileMethod("DocxUtils2: parallel image fetch and downscale")
    private void warmImageCache(List<String[]> fields) {
        MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils2", "warmImageCache");
        try {
            Set<String> guids = new HashSet<>();
            Map<String, String> inlineByHash = new HashMap<>();
            for (String[] field : fields) {
                String content = field[2];
                if (content.contains("getImage")) {
                    Matcher m = IMAGE_LINK_PATTERN.matcher(content);
                    while (m.find()) {
                        String guid = m.group(2);
                        if (!this.imageCache.containsKey(guid) && !this.imagesNotFound.contains(guid)) {
                            guids.add(guid);
                        }
                    }
                }
                if (maxImageWidth > 0 && content.contains("data:image/")) {
                    for (String uri : extractInlineDataUris(content)) {
                        inlineByHash.put(ReportImageScaler.hash(uri), uri);
                    }
                }
            }

            final Map<String, String> rawImages = new HashMap<>();
            if (!guids.isEmpty()) {
                EntityManager em = HibHelper.getInstance().getEM();
                try {
                    for (String guid : guids) {
                        try {
                            Image img = (Image) em.createQuery("SELECT i FROM Image i WHERE i.guid = :guid")
                                    .setParameter("guid", guid).getSingleResult();
                            if (img != null && img.getBase64Image() != null) {
                                if (ReportImageScaler.isReportReady(img, maxImageWidth)) {
                                    // rendition prepared at upload — no decode work left
                                    cacheImage(guid, ReportImageScaler.reportUri(img, maxImageWidth));
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
            for (Map.Entry<String, String> entry : scaledGuids.entrySet()) {
                cacheImage(entry.getKey(), entry.getValue());
            }
        } finally {
            context.end();
        }
    }

    private String centerImages(String content) {
        content = content.replaceAll("(<img\\b[^>]*?)\\s*/?>(?!</img>)", "$1></img>");
        content = content.replaceAll("<p><img", "<center><img");
        content = content.replaceAll("</img><br /></p>", "</img></center>");
        return content;
    }

    public static String replaceDateVariable(String text, String key, Date date) {
        String patternStr = "\\$\\{\\s*" + Pattern.quote(key) + "(?:\\s+([^}]+))?\\s*\\}";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String dateFormat = matcher.group(1);
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
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public String replaceFigureVariables(String text, int index) {
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
    }

    // ===== per-vuln XML replacement - same string-based logic as DocxUtils =====

    private static String replaceXml(String xml, Vulnerability v,
            HashMap<String, String> customFieldMap, HashMap<String, String> colorMap,
            HashMap<String, String> cellMap, HashMap<String, String> fillMap,
            int count, int sevIndex) {
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
        nxml = replaceAll(nxml, "category",
                v.getCategory() == null ? "UnCategorized" : v.getCategory().getName(), true);
        if (v.getClosed() == null) {
            nxml = replaceAll(nxml, "remediationStatus", "Open", false);
        } else {
            nxml = replaceAll(nxml, "remediationStatus", "Closed", false);
        }
        nxml = replaceAll(nxml, "count", "" + count, false);
        nxml = replaceAll(nxml, "loop", "", false);
        nxml = nxml.replaceAll("\\$\\{loop\\-[0-9]+\\}", "");

        if (v.getOverallStr() != null && !v.getOverallStr().equals("")) {
            nxml = replaceAll(nxml, "sevId", "" + v.getOverallStr().charAt(0) + "V" + sevIndex, false);
        } else {
            nxml = replaceAll(nxml, "sevId", "V" + sevIndex, false);
        }

        if (v.getCustomFields() != null) {
            for (CustomField cf : v.getCustomFields()) {
                if (cf.getType().getFieldType() < 3) {
                    nxml = StringUtils.replace(nxml, "${cf" + cf.getType().getVariable() + "}", CData(cf.getValue()));
                    if (customFieldMap.containsKey(cf.getType().getVariable())
                            && colorMap.containsKey(cf.getValue())) {
                        String colorMatch = customFieldMap.get(cf.getType().getVariable());
                        String color = colorMap.get(cf.getValue());
                        if (colorMatch != null && colorMatch != "" && color != null && color != "") {
                            nxml = StringUtils.replace(nxml, "w:val=\"" + colorMatch + "\"", "w:val=\"" + color + "\"");
                        }
                    }
                    if (customFieldMap.containsKey(cf.getType().getVariable())
                            && cellMap.containsKey(cf.getValue())) {
                        String colorMatch = customFieldMap.get(cf.getType().getVariable());
                        String color = cellMap.get(cf.getValue());
                        if (colorMatch != null && colorMatch != "" && color != null && color != "") {
                            nxml = StringUtils.replace(nxml, "w:fill=\"" + colorMatch + "\"", "w:fill=\"" + color + "\"");
                        }
                    }
                }
            }
        }

        nxml = StringUtils.replace(nxml, "w:color=\"FAC701\"", "w:color=\"" + colorMap.get(v.getOverallStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:color=\"FAC701\"", "w:color=\"" + colorMap.get(v.getOverallStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:color=\"FAC702\"", "w:color=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:color=\"FAC703\"", "w:color=\"" + colorMap.get(v.getImpactStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:fill=\"FAC701\"", "w:fill=\"" + cellMap.get(v.getOverallStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:fill=\"FAC702\"", "w:fill=\"" + cellMap.get(v.getLikelyhoodStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:fill=\"FAC703\"", "w:fill=\"" + cellMap.get(v.getImpactStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:val=\"FAC701\"", "w:val=\"" + colorMap.get(v.getOverallStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:val=\"FAC702\"", "w:val=\"" + colorMap.get(v.getLikelyhoodStr()) + "\"");
        nxml = StringUtils.replace(nxml, "w:val=\"FAC703\"", "w:val=\"" + colorMap.get(v.getImpactStr()) + "\"");
        return nxml;
    }

    private static String replaceAll(String original, String pattern, String replacement, Boolean wrapCDATA) {
        if (wrapCDATA) {
            return StringUtils.replace(original, "${" + pattern + "}", CData(replacement));
        } else {
            return StringUtils.replace(original, "${" + pattern + "}", replacement);
        }
    }

    private static String replaceAllCf(String original, Vulnerability v) {
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
        if (!rec.isEmpty()) {
            return replaceAllCf(rec, v);
        }
        return rec;
    }

    private static String getDescription(Vulnerability v) {
        String desc = "";
        if (v.getDescription() == null && v.getDefaultVuln() != null) {
            desc = v.getDefaultVuln().getDescription();
        } else if (v.getDescription() != null) {
            desc = v.getDescription();
        }
        if (!desc.isEmpty()) {
            return replaceAllCf(desc, v);
        }
        return desc;
    }

    private static String getDetails(Vulnerability v) {
        String details = "";
        if (v.getDetails() != null) {
            return v.getDetails();
        }
        if (!details.isEmpty()) {
            return replaceAllCf(details, v);
        }
        return details;
    }

    // ===== stubs - features not yet ported =====

    /**
     * Hyperlink rewriting is not yet ported. Templates that use
     * ${cf<var> link} placeholders for clickable URLs will surface them
     * as plain text until this is implemented.
     */
    private String replaceHyperlinks(String xml, Map<String, String> map) {
        return xml;
    }

    /**
     * ${pageBreak} placeholders and ${TOC} generation are not yet ported.
     * The caller can still produce a ToC update via LibreOffice on the
     * final docx - which is the same approach the production flow uses when
     * finalize() is invoked.
     */
    private String removePageBreaks(String xml) {
        return xml;
    }
}
