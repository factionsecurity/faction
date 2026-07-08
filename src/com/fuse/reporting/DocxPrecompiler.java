package com.fuse.reporting;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
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

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.RFonts;

import com.fuse.dao.CustomField;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.Vulnerability;
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
 * The pipeline splits the original wrapHTML flow:
 *
 * At save time (this class):
 *   raw HTML → resolve ${cf..} custom fields → resolve getImage links →
 *   jtidy → downscale images → XHTMLImporter.convert() → marshal to XML
 *   string → store on Vulnerability
 *
 * Assessment-level variables (${asmtName}, ${today}, etc.) are left as
 * literal text inside <w:t> nodes — the importer treats them as plain
 * text. At report time, DocxUtils.wrapHTML resolves them via string
 * replacement on the cached XML, then unmarshals to JAXB nodes.
 *
 * Image rIds produced by the importer are scoped to a throwaway scratch
 * package. cachedImageRIds stores the mapping (importer rId → image
 * bytes) so report-time code can allocate fresh rIds in the real report
 * package and rewrite the references.
 *
 * Cache invalidation: the content hash (SHA-256 of font+content) is
 * stored alongside the XML. At report time, if the hash doesn't match
 * the current content, the cache is stale and wrapHTML falls back to
 * live conversion.
 */
public class DocxPrecompiler {

    private static final Pattern IMAGE_LINK_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']getImage\\?id(=|&#61;)[0-9]+:([^\"'\\s>]+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE);

    private final String font;
    private final String customCSS;
    private final int maxImageWidth;

    // scratch package reused across all three field conversions for one
    // vuln — the importer needs a package to attach image parts to
    private WordprocessingMLPackage scratchPackage;

    // image rIds captured from the scratch package after conversion. The
    // report-time fast path uses these to copy bytes into the report
    // package and remap rIds in the cached XML.
    //
    // Keyed by importer rId (e.g. "rId3"). Multiple fields on the same
    // vuln that reference the same image will share the same rId because
    // they share the same scratch package.
    private Map<String, byte[]> capturedImages = new HashMap<>();
    private Map<String, String> capturedContentTypes = new HashMap<>();

    public DocxPrecompiler(String font, String customCSS) {
        this.font = font == null ? "Calibri" : font;
        this.customCSS = customCSS == null ? "" : customCSS;
        this.maxImageWidth = ReportImageScaler.configuredMaxWidth();
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

        // custom-field substitution for details (getDescription/getRecommendation
        // already applied it via replaceAllCf)
        details = replaceAllCf(details, v);

        String descHash = hash(font, desc);
        String recHash = hash(font, rec);
        String detailsHash = hash(font, details);

        boolean needScratch = false;
        if (desc != null && !desc.isEmpty() && !descHash.equals(v.getCachedDescHash())) {
            needScratch = true;
        }
        if (rec != null && !rec.isEmpty() && !recHash.equals(v.getCachedRecHash())) {
            needScratch = true;
        }
        if (!details.isEmpty() && !detailsHash.equals(v.getCachedDetailsHash())) {
            needScratch = true;
        }

        if (!needScratch) {
            return false;
        }

        try {
            this.scratchPackage = WordprocessingMLPackage.createPackage();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

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

        return updated;
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
        if (v == null) return;
        try {
            DocxPrecompiler pre = new DocxPrecompiler(font, customCSS);
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
        // - image link resolution (getImage?id= → data URI)
        // - inline image downscaling
        //
        // Do NOT apply:
        // - assessment-level variables (leave ${asmtName} as literal text)
        // - report extensions (assessment-scoped, can't pre-compile)
        // - loopReplace (assessment-scoped)

        content = content.replaceAll("\n", "<br />");
        content = content.replaceAll("<p><br /></p>", "");
        content = content.replaceAll("<blockquote>", "<center class='figure'>");
        content = content.replaceAll("</blockquote>", "</center>");
        content = FSUtils.jtidy(content);

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
                        String base64 = ReportImageScaler.downscaleDataUri(img.getBase64Image(), maxImageWidth);
                        resolved.put(guid, base64);
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
    // new image parts and capture their bytes. The rIds are stored so
    // report-time code can copy bytes into the real report package and
    // remap rId references in the cached XML.
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
                continue; // already captured (shared across fields)
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

    /**
     * Returns the captured image data (rId → bytes) collected during
     * compilation. Report-time code uses this to copy images into the
     * report package and remap rIds.
     */
    public Map<String, byte[]> getCapturedImages() {
        return capturedImages;
    }

    public Map<String, String> getCapturedContentTypes() {
        return capturedContentTypes;
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

    // normalizes namespace prefixes to the standard OOXML prefixes so
    // the report-time splitter can recognize <w:p> elements. The docx4j
    // marshaller assigns arbitrary prefixes (ns2, ns3, etc.) when
    // marshalling standalone nodes outside a document context.
    private static String normalizeNamespaces(String xml) {
        int wIdx = xml.indexOf("=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"");
        if (wIdx < 0) return xml;
        int xmlnsStart = xml.lastIndexOf("xmlns:", wIdx - 1);
        if (xmlnsStart < 0) return xml;
        int colonIdx = xmlnsStart + 6;
        int nsEnd = colonIdx;
        while (nsEnd < wIdx - 1 && xml.charAt(nsEnd) != '=') {
            nsEnd++;
        }
        String prefix = xml.substring(colonIdx, nsEnd);
        if ("w".equals(prefix)) return xml;
        xml = xml.replace("xmlns:" + prefix + "=", "xmlns:w=");
        xml = xml.replace("<" + prefix + ":", "<w:");
        xml = xml.replace("</" + prefix + ":", "</w:");
        xml = xml.replace(" " + prefix + ":", " w:");
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

    // bump this when the cached XML format changes (namespace normalization,
    // snippet splitting logic, etc.) to invalidate all existing caches and
    // force recompilation on the next migration/save
    private static final String CACHE_VERSION = "v5";

    // SHA-256 hash of font+content+cacheVersion for cache invalidation
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

    // returns true if the content contains any kind of image reference —
    // either getImage?id=... links (uploaded screenshots) or inline
    // data:image/... base64 URIs (pasted screenshots). Fields with images
    // can't be pre-compiled because the importer's rIds are package-scoped.
    private static boolean containsImageContent(String content) {
        if (content == null || content.isEmpty()) return false;
        return content.contains("getImage") || content.contains("data:image/");
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
