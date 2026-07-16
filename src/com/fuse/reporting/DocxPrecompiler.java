package com.fuse.reporting;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.RFonts;

import com.fuse.dao.CustomField;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Assessment;
import com.fuse.dao.Image;
import com.fuse.dao.Vulnerability;
import com.fuse.extenderapi.Extensions;
import com.fuse.utils.FSUtils;
import com.fuse.utils.LoggingConfig;
import com.fuse.utils.ReportImageScaler;

import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;

/**
 * Pre-compiles vulnerability HTML fields (desc/rec/details) into serialized
 * OOXML at save time, so report generation can skip the expensive
 * XHTMLImporterImpl.convert() call.
 *
 * The cached XML must be SELF-CONTAINED — a paragraph that references
 * anything by id is worthless once it leaves the package the conversion
 * ran in. Two kinds of references are made portable:
 *
 * Images: the importer attaches image parts to the scratch package and
 * references them by rId. The image bytes are captured and embedded
 * directly into the cached XML as data URIs (r:embed="data:image/...");
 * report-time code creates real parts in the report package and swaps in
 * fresh rIds.
 *
 * Numbering: a list paragraph carries only <w:numPr><w:numId w:val="N"/>,
 * while what N MEANS (bullet vs decimal) lives in the package's
 * numbering part. The v1 cache stored the bare numId and dropped the
 * definitions — spliced into a report whose template owned those ids,
 * bullet lists rendered as continued decimal numbers. Now the scratch
 * package's entire numbering part is serialized into the cache payload
 * (behind NUM_DEFS_MARKER) and the field XML's numId references are
 * rewritten to FCT-NUM-&lt;id&gt; tokens. At report time
 * DocxUtils.spliceCachedNumbering() re-creates every definition under
 * freshly allocated ids and substitutes the tokens, so the template's
 * own numbering is never referenced or disturbed. Each field gets its
 * own scratch package so the captured numbering is exactly that field's.
 *
 * Assessment-level variables (${asmtName}, ${today}, etc.) are left as
 * literal text inside <w:t> nodes — the importer treats them as plain
 * text. At report time, DocxUtils resolves them via string replacement
 * on the cached XML, then unmarshals to JAXB nodes.
 *
 * Cache invalidation: the content hash (SHA-256 of version+font+content)
 * is stored alongside the XML. At report time, if the hash doesn't match
 * the current content, the cache is stale and wrapHTML falls back to
 * live conversion. Bumping CACHE_VERSION invalidates every existing
 * cache — including all v1-era rows still in the database.
 */
public class DocxPrecompiler {

    private static final Pattern IMAGE_LINK_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']getImage\\?id(=|&#61;)[0-9]+:([^\"'\\s>]+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE);

    /**
     * Separates the field's paragraph XML from the serialized numbering
     * definitions in the cached payload. An XML comment can never appear
     * inside marshalled OOXML, so the split is unambiguous.
     */
    public static final String NUM_DEFS_MARKER = "<!--FCT-NUM-DEFS-->";

    private final String font;
    private final String customCSS;
    private final int maxImageWidth;

    // fresh per field (see compileField) — numbering definitions captured
    // from it must belong to exactly one field
    private WordprocessingMLPackage scratchPackage;

    // image rIds captured from the scratch package after conversion,
    // keyed by importer rId (e.g. "rId3"); reset per field
    private Map<String, byte[]> capturedImages = new HashMap<>();
    private Map<String, String> capturedContentTypes = new HashMap<>();

    // the parent assessment — needed to run report extensions at pre-compile
    // time so their output is baked into the cached XML
    private final Assessment assessment;
    // report extension instance, lazily initialized
    private Extensions reportExtension;

    public DocxPrecompiler(String font, String customCSS) {
        this(font, customCSS, null);
    }

    public DocxPrecompiler(String font, String customCSS, Assessment assessment) {
        this.font = font == null ? "Calibri" : font;
        this.customCSS = customCSS == null ? "" : customCSS;
        this.maxImageWidth = ReportImageScaler.configuredMaxWidth();
        this.assessment = assessment;
    }

    /**
     * Pre-compiles all three HTML fields on a vulnerability and stores the
     * results. Only fields whose content has changed (hash mismatch) are
     * reconverted; unchanged fields keep their existing cache.
     *
     * @return true if any cache was updated
     */
    public boolean compile(Vulnerability v) {
        boolean updated = false;

        // resolve custom fields into the content before hashing — this is
        // what getDescription/getRecommendation/getDetails do at report time
        String desc = getDescription(v);
        String rec = getRecommendation(v);
        String details = v.getDetails() != null ? v.getDetails() : "";
        details = replaceAllCf(details, v);

        String descHash = hash(font, desc);
        String recHash = hash(font, rec);
        String detailsHash = hash(font, details);

        if (desc != null && !desc.isEmpty() && !descHash.equals(v.getCachedDescHash())) {
            try {
                String xml = compileField(desc, "desc");
                v.setCachedDescXml(xml);
                v.setCachedDescHash(descHash);
                updated = true;
            } catch (Exception e) {
                e.printStackTrace();
                // leave cache stale; report-time will fall back to live
                v.setCachedDescXml(null);
                v.setCachedDescHash(null);
            }
        }

        if (rec != null && !rec.isEmpty() && !recHash.equals(v.getCachedRecHash())) {
            try {
                String xml = compileField(rec, "rec");
                v.setCachedRecXml(xml);
                v.setCachedRecHash(recHash);
                updated = true;
            } catch (Exception e) {
                e.printStackTrace();
                v.setCachedRecXml(null);
                v.setCachedRecHash(null);
            }
        }

        if (!details.isEmpty() && !detailsHash.equals(v.getCachedDetailsHash())) {
            try {
                String xml = compileField(details, "details");
                v.setCachedDetailsXml(xml);
                v.setCachedDetailsHash(detailsHash);
                updated = true;
            } catch (Exception e) {
                e.printStackTrace();
                v.setCachedDetailsXml(null);
                v.setCachedDetailsHash(null);
            }
        }

        // type-3 (HTML) custom fields — entries with a matching hash are
        // carried over, changed/new values are recompiled
        try {
            String rebuiltCf = compileCustomFields(v);
            String existing = v.getCachedCfXml();
            if (rebuiltCf == null ? existing != null : !rebuiltCf.equals(existing)) {
                v.setCachedCfXml(rebuiltCf);
                updated = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            v.setCachedCfXml(null);
        }

        return updated;
    }

    /**
     * One cached entry per marker: {@code <!--FCT-CF var hash-->payload}.
     * The payload is the same self-contained format compileField produces
     * (paragraph XML, embedded images, numbering behind NUM_DEFS_MARKER).
     */
    public static final String CF_ENTRY_PREFIX = "<!--FCT-CF ";

    private String compileCustomFields(Vulnerability v) {
        if (v.getCustomFields() == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (CustomField cf : v.getCustomFields()) {
            if (cf.getType() == null || cf.getType().getFieldType() != 3) {
                continue;
            }
            String value = cf.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            String var = cf.getType().getVariable();
            String h = hash(font, value);
            String[] prior = findCfEntry(v.getCachedCfXml(), var);
            String payload;
            if (prior != null && prior[0].equals(h)) {
                payload = prior[1];
            } else {
                try {
                    payload = compileField(value, var);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue; // this field falls back to live conversion
                }
            }
            sb.append(CF_ENTRY_PREFIX).append(var).append(' ').append(h).append("-->").append(payload);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /**
     * Finds the cached entry for a custom-field variable.
     *
     * @return {hash, payload}, or null when the variable has no entry
     */
    public static String[] findCfEntry(String cachedCfXml, String variable) {
        if (cachedCfXml == null || variable == null) {
            return null;
        }
        int idx = 0;
        while ((idx = cachedCfXml.indexOf(CF_ENTRY_PREFIX, idx)) != -1) {
            int headEnd = cachedCfXml.indexOf("-->", idx);
            if (headEnd < 0) {
                return null;
            }
            String head = cachedCfXml.substring(idx + CF_ENTRY_PREFIX.length(), headEnd);
            int nextEntry = cachedCfXml.indexOf(CF_ENTRY_PREFIX, headEnd);
            int sp = head.lastIndexOf(' ');
            if (sp > 0 && head.substring(0, sp).equals(variable)) {
                String payload = cachedCfXml.substring(headEnd + 3,
                        nextEntry < 0 ? cachedCfXml.length() : nextEntry);
                return new String[] { head.substring(sp + 1), payload };
            }
            idx = nextEntry < 0 ? cachedCfXml.length() : nextEntry;
        }
        return null;
    }

    /**
     * Convenience method to pre-compile a vulnerability's HTML fields
     * after it has been persisted. Looks up report options (font + CSS),
     * runs the precompiler, and persists the cached XML back.
     *
     * Safe to call from save paths — if anything fails, the cache is
     * simply left stale and report-time will fall back to live conversion.
     */
    public static void precompileAndPersist(Vulnerability v, String font, String customCSS) {
        precompileAndPersist(v, font, customCSS, null);
    }

    public static void precompileAndPersist(Vulnerability v, String font, String customCSS, Assessment assessment) {
        if (v == null) return;
        try {
            DocxPrecompiler pre = new DocxPrecompiler(font, customCSS, assessment);
            if (pre.compile(v)) {
                // persist the cached fields — use a fresh EM from the EMF
                // to avoid closing the shared HibHelper singleton that
                // report generation might be using concurrently
                EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
                try {
                    HibHelper.getInstance().preJoin();
                    em.joinTransaction();
                    em.merge(v);
                    HibHelper.getInstance().commit();
                } finally {
                    em.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hashes content the same way compile() does, so callers can check
     * staleness without running the precompiler.
     */
    public static String contentHash(String font, String content) {
        return hash(font, content);
    }

    // ===== per-field compilation =====

    private String compileField(String content, String className) throws Docx4JException {
        // Apply per-vuln-pre-compilable transformations:
        // - \n → <br/>
        // - blockquote → center.figure
        // - jtidy (HTML cleanup)
        // - misnested-list repair (ul directly inside ol, editor artifact)
        // - image link resolution (getImage?id= → data URI)
        // - inline image downscaling
        //
        // Do NOT apply:
        // - assessment-level variables (leave ${asmtName} as literal text)
        // - loopReplace (assessment-scoped)

        // fresh scratch per field: the numbering part captured below must
        // contain exactly this field's list definitions and nothing else
        try {
            this.scratchPackage = WordprocessingMLPackage.createPackage();
        } catch (Exception e) {
            throw new Docx4JException("scratch package creation failed", e);
        }
        this.capturedImages.clear();
        this.capturedContentTypes.clear();

        content = content.replaceAll("\n", "<br />");
        content = content.replaceAll("<p><br /></p>", "");
        content = content.replaceAll("<blockquote>", "<center class='figure'>");
        content = content.replaceAll("</blockquote>", "</center>");
        content = FSUtils.jtidy(content);
        content = DocxUtils.hoistMisnestedLists(content);

        // run report extensions at pre-compile time so their output (e.g.
        // injected charts, cross-references) is baked into the cached XML.
        // Only runs when the content contains a ${ placeholder; the
        // short-circuit inside updateReport() avoids the expensive clone.
        if (assessment != null && content.contains("${")) {
            if (reportExtension == null) {
                reportExtension = new Extensions(HibHelper.getInstance().getEMF(),
                        Extensions.EventType.REPORT_MANAGER);
            }
            if (reportExtension.isExtended()) {
                content = reportExtension.updateReport(assessment, content);
            }
        }

        // resolve getImage?id=... links to base64 data URIs
        content = resolveImageLinks(content);

        // downscale oversized inline images
        if (maxImageWidth > 0 && content.contains("data:image/")) {
            content = downscaleInlineImages(content);
        }

        // build full HTML page and convert
        String html = htmlPageHead(customCSS) + "<div class='" + className + "'>" + content + "</div></body></html>";

        XHTMLImporterImpl importer = newImporter();
        List<Object> converted = importer.convert(html, null);
        captureImages();

        // marshal to XML string, stripping <?xml?> declarations
        String xml = marshalNodesToXml(converted);

        // replace scratch-package rId references with the actual image data
        // URIs. At report time, DocxUtils creates real image parts in the
        // report package and replaces the data URIs with valid rIds
        xml = embedImageDataUris(xml);

        // capture the numbering definitions this conversion created and
        // rewrite the paragraphs' numId references to portable tokens —
        // see the class comment for why bare numIds must never be cached
        NumberingDefinitionsPart ndp = scratchPackage.getMainDocumentPart().getNumberingDefinitionsPart();
        if (ndp != null && ndp.getContents() != null && !ndp.getContents().getNum().isEmpty()) {
            Numbering numbering = ndp.getContents();
            for (Numbering.Num n : numbering.getNum()) {
                xml = xml.replace("w:numId w:val=\"" + n.getNumId() + "\"",
                        "w:numId w:val=\"FCT-NUM-" + n.getNumId() + "\"");
            }
            xml = xml + NUM_DEFS_MARKER + XmlUtils.marshaltoString(numbering, true, false);
        }

        return xml;
    }

    // replaces r:embed="rIdN" with r:embed="data:image/..." using the
    // captured image bytes. This makes the cached XML self-contained —
    // no dependency on the scratch package's relationship table.
    private String embedImageDataUris(String xml) {
        if (capturedImages.isEmpty()) return xml;
        for (Map.Entry<String, byte[]> entry : capturedImages.entrySet()) {
            String rId = entry.getKey();
            byte[] bytes = entry.getValue();
            String contentType = capturedContentTypes.get(rId);
            if (contentType == null) contentType = "image/png";
            String dataUri = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
            // r:embed="rId3" → r:embed="data:image/png;base64,..."
            xml = xml.replace("r:embed=\"" + rId + "\"", "r:embed=\"" + dataUri + "\"");
            // also handle r:link (rare)
            xml = xml.replace("r:link=\"" + rId + "\"", "r:link=\"" + dataUri + "\"");
        }
        return xml;
    }

    // ===== image link resolution (getImage?id=...) =====

    private String resolveImageLinks(String text) {
        if (text == null || text.isEmpty() || !text.contains("getImage")) {
            return text;
        }

        // normalize img tags
        text = text.replaceAll("(<img\\b[^>]*?)\\s*/?>(?!</img>)", "$1></img>");
        text = text.replaceAll("<p><img", "<center><img");
        text = text.replaceAll("</img><br /></p>", "</img></center>");

        // remove undefined image links
        text = text.replaceAll("<img[^>]*src=[\"']getImage\\?id(=|&#61;)undefined[\"'][^>]*>(</img>)?", "");

        Pattern imagePattern = IMAGE_LINK_PATTERN;
        Set<String> referencedGuids = new HashSet<>();
        Matcher matcher = imagePattern.matcher(text);
        while (matcher.find()) {
            referencedGuids.add(matcher.group(2));
        }
        if (referencedGuids.isEmpty()) {
            return text;
        }

        Map<String, String> resolved = resolveImages(referencedGuids);

        StringBuffer result = new StringBuffer();
        matcher = imagePattern.matcher(text);
        while (matcher.find()) {
            String guid = matcher.group(2);
            String base64Image = resolved.get(guid);
            if (base64Image != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement("<img src=\"" + base64Image + "\" >"));
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Map<String, String> resolveImages(Set<String> guids) {
        Map<String, String> resolved = new HashMap<>();
        // use a fresh EM from the EMF, NOT the shared HibHelper.getEM()
        // singleton — closing the singleton breaks any concurrent report
        // generation that also uses it
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            for (String guid : guids) {
                try {
                    Image img = (Image) em.createQuery("SELECT i FROM Image i WHERE i.guid = :guid")
                            .setParameter("guid", guid).getSingleResult();
                    if (img != null && img.getBase64Image() != null) {
                        resolved.put(guid, ReportImageScaler.reportUri(img, maxImageWidth));
                    }
                } catch (Exception e) {
                    // image not found
                }
            }
        } finally {
            em.close();
        }
        return resolved;
    }

    // ===== scratch package image capture =====

    // After each convert() call, scan the scratch package's relationships for
    // new image parts and capture their bytes so they can be embedded into
    // the cached XML as data URIs.
    private void captureImages() {
        if (scratchPackage == null) return;
        RelationshipsPart rels = scratchPackage.getMainDocumentPart().getRelationshipsPart();
        if (rels == null || rels.getRelationships() == null) return;

        for (Relationship r : rels.getRelationships().getRelationship()) {
            if (r.getType() == null || !r.getType().endsWith("/image")) {
                continue;
            }
            String rId = r.getId();
            if (capturedImages.containsKey(rId)) {
                continue;
            }
            try {
                org.docx4j.openpackaging.parts.Part part = rels.getPart(r);
                if (part instanceof BinaryPart) {
                    BinaryPart bp = (BinaryPart) part;
                    byte[] bytes = bp.getBytes();
                    capturedImages.put(rId, bytes);
                    capturedContentTypes.put(rId, bp.getContentType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===== helpers (mirrors DocxUtils) =====

    private String htmlPageHead(String css) {
        return "<!DOCTYPE html><html><head>"
                + "<style>html{padding:0;margin:0;margin-right:0px;}\r\nbody{padding:0;margin:0;font-family:"
                + this.font + ";}\r\n" + css + "</style>" + "</head><body>";
    }

    private XHTMLImporterImpl newImporter() {
        LoggingConfig.configureOpenHTMLTopDFLogging();
        XHTMLImporterImpl xhtml = new XHTMLImporterImpl(scratchPackage);
        RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
        rfonts.setAscii(this.font);
        XHTMLImporterImpl.addFontMapping("Arial", rfonts);
        XHTMLImporterImpl.addFontMapping("arial", rfonts);
        return xhtml;
    }

    private static String marshalNodesToXml(List<Object> nodes) {
        StringBuilder sb = new StringBuilder();
        for (Object node : nodes) {
            try {
                String xml = XmlUtils.marshaltoString(node, false, false);
                if (xml.startsWith("<?xml")) {
                    int close = xml.indexOf("?>");
                    if (close >= 0) {
                        xml = xml.substring(close + 2);
                    }
                }
                // normalize namespace prefixes to the standard w: prefix
                // so splitTopLevelParagraphs can find <w:p> at report time.
                // docx4j's standalone marshaller may use arbitrary prefixes
                // like <ns3:p> which the splitter doesn't recognize.
                xml = normalizeNamespaces(xml);
                sb.append(xml);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return sb.toString();
    }

    // Normalizes namespace prefixes to the standard OOXML prefixes so
    // the report-time splitter can recognize <w:p> elements. The docx4j
    // marshaller assigns arbitrary prefixes (ns2, ns3, etc.) when
    // marshalling standalone nodes outside a document context.
    //
    // Every replacement is anchored to XML structure — element names via
    // the '<'/'</' that cannot appear unescaped in text content, and
    // attribute names via the trailing '="'. A bare " prefix:" replace
    // once rewrote TEXT: with an off-by-one that read the usual "w"
    // prefix as "", every " :" in user content became " w:".
    private static String normalizeNamespaces(String xml) {
        int wIdx = xml.indexOf("=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"");
        if (wIdx < 0) return xml;
        int xmlnsStart = xml.lastIndexOf("xmlns:", wIdx - 1);
        if (xmlnsStart < 0) return xml;
        int colonIdx = xmlnsStart + 6;
        int nsEnd = colonIdx;
        while (nsEnd < wIdx && xml.charAt(nsEnd) != '=') {
            nsEnd++;
        }
        String prefix = xml.substring(colonIdx, nsEnd);
        if (prefix.isEmpty() || "w".equals(prefix)) return xml;
        xml = xml.replace("xmlns:" + prefix + "=\"", "xmlns:w=\"");
        xml = xml.replace("<" + prefix + ":", "<w:");
        xml = xml.replace("</" + prefix + ":", "</w:");
        // attribute prefixes: whitespace + prefix + ":" + name + "=\""
        xml = xml.replaceAll("(\\s)" + Pattern.quote(prefix) + ":([A-Za-z][A-Za-z0-9]*=\")", "$1w:$2");
        return xml;
    }

    private String downscaleInlineImages(String content) {
        List<String> uris = extractInlineDataUris(content);
        if (uris.isEmpty()) return content;
        StringBuilder sb = new StringBuilder(content);
        for (String uri : uris) {
            String downscaled = ReportImageScaler.downscaleDataUri(uri, maxImageWidth);
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
            if (idx == 0) { idx += 1; continue; }
            char quote = content.charAt(idx - 1);
            if (quote != '"' && quote != '\'') { idx += 1; continue; }
            int end = content.indexOf(quote, idx);
            if (end == -1) break;
            uris.add(content.substring(idx, end));
            idx = end;
        }
        return uris;
    }

    // Bump this when the cached XML format changes to invalidate all
    // existing caches and force recompilation on the next migration/save.
    // v7: numbering definitions captured + tokenized numIds — v6-era
    // caches carried bare scratch-package numIds that collided with the
    // report template's numbering (bullets rendered as decimal).
    // v8: normalizeNamespaces text-corruption fix — v7 rows may have
    // " :" in user text stored as " w:".
    private static final String CACHE_VERSION = "v8";

    // SHA-256 hash of version+font+content for cache invalidation
    private static String hash(String font, String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = CACHE_VERSION + "\0" + font + "\0" + (content == null ? "" : content);
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            return "" + System.currentTimeMillis(); // fallback — always stale
        }
    }

    private static String replaceAllCf(String original, Vulnerability v) {
        if (v.getCustomFields() != null) {
            for (CustomField cf : v.getCustomFields()) {
                try {
                    original = StringUtils.replace(original,
                            "${cf" + cf.getType().getVariable() + "}",
                            cf.getValue() == null ? "" : cf.getValue());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return original;
    }

    // Mirrors DocxUtils.getDescription/getRecommendation — resolves
    // default-vulnerability fallback and applies custom-field substitution.
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
}
