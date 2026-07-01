package com.fuse.actions.assessment;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.fuse.dao.FinalReport;
import com.fuse.dao.FinalReportVariant;

/**
 * Unit tests for the UploadReport action logic at the entity level.
 *
 * The action itself requires a Struts container (session, EntityManager, etc.),
 * so these tests exercise the entity mutations the action performs — variant
 * creation, password handling, variantCount — without spinning up Struts or a
 * database.
 */
public class UploadReportLogicTest {

    // =========================================================================
    // File type detection (mirrors the isPdf / isDocx logic in UploadReport)
    // =========================================================================

    @Test
    public void testFileType_pdfContentType() {
        assertTrue(isPdf("application/pdf", "report"));
        assertFalse(isDocx("application/pdf", "report"));
    }

    @Test
    public void testFileType_pdfExtension() {
        assertTrue(isPdf(null, "report.pdf"));
        assertFalse(isDocx(null, "report.pdf"));
    }

    @Test
    public void testFileType_docxContentType() {
        assertTrue(isDocx("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "report"));
        assertFalse(isPdf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "report"));
    }

    @Test
    public void testFileType_docxExtension() {
        assertTrue(isDocx(null, "report.docx"));
        assertFalse(isPdf(null, "report.docx"));
    }

    @Test
    public void testFileType_nullContentTypeFallsBackToExtension() {
        assertTrue(isPdf(null, "myreport.pdf"));
        assertTrue(isDocx(null, "myreport.docx"));
    }

    @Test
    public void testFileType_emptyContentTypeFallsBackToExtension() {
        assertTrue(isPdf("", "myreport.pdf"));
        assertTrue(isDocx("", "myreport.docx"));
    }

    @Test
    public void testFileType_unsupportedIsNeitherPdfNorDocx() {
        assertFalse(isPdf("text/plain", "report.txt"));
        assertFalse(isDocx("text/plain", "report.txt"));
    }

    @Test
    public void testFileType_caseInsensitiveExtension() {
        assertTrue(isPdf(null, "report.PDF"));
        assertTrue(isDocx(null, "report.DOCX"));
    }

    // =========================================================================
    // New FinalReport created on first upload — PDF
    // =========================================================================

    @Test
    public void testNewReport_pdfUpload_createsOneVariant() {
        FinalReport fr = simulatePdfUpload(null, "pdfb64data");

        assertEquals("PDF upload to new report should create 1 variant", 1, fr.getVariants().size());
        assertEquals("Variant fileType should be pdf", "pdf", fr.getVariants().get(0).getFileType());
        assertEquals("Variant content should match uploaded b64", "pdfb64data",
                fr.getVariants().get(0).getBase64Content());
        assertEquals("variantCount should be 1", 1, fr.getVariantCount());
    }

    @Test
    public void testNewReport_pdfUpload_retestFalse() {
        FinalReport fr = simulatePdfUpload(null, "pdfb64");
        assertFalse("Newly created FinalReport should have retest=false", fr.getRetest());
    }

    @Test
    public void testNewReport_pdfUpload_filenameIsUUID() {
        FinalReport fr = simulatePdfUpload(null, "pdfb64");
        assertNotNull("Filename should be set", fr.getFilename());
        // UUID.fromString throws if invalid
        UUID.fromString(fr.getFilename());
    }

    @Test
    public void testNewReport_pdfUpload_gentimeSet() {
        FinalReport fr = simulatePdfUpload(null, "pdfb64");
        assertNotNull("Gentime should be set", fr.getGentime());
    }

    // =========================================================================
    // New FinalReport created on first upload — DOCX (free edition: 1 variant)
    // =========================================================================

    @Test
    public void testNewReport_docxUpload_createsOneVariant() {
        FinalReport fr = simulateDocxUpload(null, "docxb64data");

        assertEquals("DOCX upload to new report (free) should create 1 variant", 1, fr.getVariants().size());
        assertEquals("docx", fr.getVariants().get(0).getFileType());
        assertEquals("docxb64data", fr.getVariants().get(0).getBase64Content());
        assertEquals(1, fr.getVariantCount());
    }

    @Test
    public void testNewReport_docxUpload_noPasswordSet() {
        // Free edition: no PDF generated, so no encrypted password
        FinalReport fr = simulateDocxUpload(null, "docxb64");
        assertNull("No password should be set for free DOCX upload", fr.getEncryptedReportPassword());
    }

    // =========================================================================
    // Re-upload over an existing report — variants replaced
    // =========================================================================

    @Test
    public void testReupload_clearsExistingVariants() {
        FinalReport existing = new FinalReport();
        existing.setFilename("old-guid");
        existing.setRetest(false);
        FinalReportVariant oldVariant = new FinalReportVariant();
        oldVariant.setFileType("docx");
        oldVariant.setBase64Content("oldcontent");
        existing.getVariants().add(oldVariant);
        existing.setVariantCount(1);

        FinalReport updated = simulatePdfUpload(existing, "newpdfb64");

        assertEquals("Re-upload must replace variants, not append", 1, updated.getVariants().size());
        assertEquals("pdf", updated.getVariants().get(0).getFileType());
        assertEquals("newpdfb64", updated.getVariants().get(0).getBase64Content());
    }

    @Test
    public void testReupload_gentimeUpdated() {
        FinalReport existing = new FinalReport();
        Date oldDate = new Date(0); // epoch
        existing.setGentime(oldDate);

        FinalReport updated = simulatePdfUpload(existing, "pdfb64");

        assertNotEquals("Gentime must be updated on re-upload", oldDate, updated.getGentime());
    }

    // =========================================================================
    // Password handling on re-upload
    // =========================================================================

    @Test
    public void testReupload_pdfKeepsExistingPassword() {
        // Uploading a PDF should preserve whatever encrypted password was on the report
        FinalReport existing = new FinalReport();
        existing.setEncryptedReportPassword("encrypted:existingpw");

        FinalReport updated = simulatePdfUpload(existing, "pdfb64");

        assertEquals("PDF re-upload must preserve existing encrypted password",
                "encrypted:existingpw", updated.getEncryptedReportPassword());
    }

    @Test
    public void testReupload_docxClearsPassword() {
        // Free edition DOCX: no PDF generated, password is no longer applicable
        FinalReport existing = new FinalReport();
        existing.setEncryptedReportPassword("encrypted:existingpw");

        FinalReport updated = simulateDocxUpload(existing, "docxb64");

        assertNull("DOCX re-upload (free edition) must clear the encrypted password",
                updated.getEncryptedReportPassword());
    }

    @Test
    public void testReupload_docxToReportWithNoPassword_passwordRemainsNull() {
        FinalReport existing = new FinalReport();
        // no password set

        FinalReport updated = simulateDocxUpload(existing, "docxb64");

        assertNull("Password should remain null when there was nothing to clear", updated.getEncryptedReportPassword());
    }

    // =========================================================================
    // Backward compat: getEffectiveVariants() works after an upload
    // =========================================================================

    @Test
    public void testEffectiveVariants_afterPdfUpload() {
        FinalReport fr = simulatePdfUpload(null, "pdfb64");

        // getEffectiveVariants() must return the stored variants (not fall back to legacy)
        assertEquals(1, fr.getEffectiveVariants().size());
        assertEquals("pdf", fr.getEffectiveVariants().get(0).getFileType());
    }

    @Test
    public void testEffectiveVariants_afterDocxUpload() {
        FinalReport fr = simulateDocxUpload(null, "docxb64");

        assertEquals(1, fr.getEffectiveVariants().size());
        assertEquals("docx", fr.getEffectiveVariants().get(0).getFileType());
    }

    @Test
    public void testEffectiveVariants_backwardCompat_legacyRecordUnaffected() {
        // Reports generated before the multi-format branch have base64EncodedPdf set
        // and an empty variants list. getEffectiveVariants() must synthesize a variant.
        FinalReport legacy = new FinalReport();
        legacy.setFileType("docx");
        legacy.setBase64EncodedPdf("legacycontent");

        assertEquals("Legacy record should synthesize 1 variant", 1, legacy.getEffectiveVariants().size());
        assertEquals("docx", legacy.getEffectiveVariants().get(0).getFileType());
        assertEquals("legacycontent", legacy.getEffectiveVariants().get(0).getBase64Content());
    }

    // =========================================================================
    // helpers — replicates the entity mutations from UploadReport.uploadReport()
    // =========================================================================

    /**
     * Simulates a PDF upload. Pass {@code existing=null} to simulate first upload.
     */
    private FinalReport simulatePdfUpload(FinalReport existing, String b64) {
        FinalReport fr;
        if (existing == null) {
            fr = new FinalReport();
            fr.setRetest(false);
            fr.setFilename(UUID.randomUUID().toString());
            fr.setGentime(new Date());
        } else {
            fr = existing;
            fr.getVariants().clear();
            fr.setGentime(new Date());
            // PDF upload: password preserved (not cleared)
        }

        FinalReportVariant variant = new FinalReportVariant();
        variant.setFileType("pdf");
        variant.setBase64Content(b64);
        fr.getVariants().add(variant);
        fr.setVariantCount(1);
        return fr;
    }

    /**
     * Simulates a DOCX upload in the free edition (single variant, no PDF conversion).
     * Pass {@code existing=null} to simulate first upload.
     */
    private FinalReport simulateDocxUpload(FinalReport existing, String b64) {
        FinalReport fr;
        if (existing == null) {
            fr = new FinalReport();
            fr.setRetest(false);
            fr.setFilename(UUID.randomUUID().toString());
            fr.setGentime(new Date());
        } else {
            fr = existing;
            fr.getVariants().clear();
            fr.setGentime(new Date());
            fr.setEncryptedReportPassword(null); // DOCX in free: no PDF, no password
        }

        FinalReportVariant variant = new FinalReportVariant();
        variant.setFileType("docx");
        variant.setBase64Content(b64);
        fr.getVariants().add(variant);
        fr.setVariantCount(1);
        return fr;
    }

    // --- file type detection helpers (mirrors UploadReport logic) ---

    private boolean isPdf(String contentType, String filename) {
        String ct = contentType == null ? "" : contentType.toLowerCase();
        String fn = filename == null ? "" : filename.toLowerCase();
        return ct.contains("pdf") || fn.endsWith(".pdf");
    }

    private boolean isDocx(String contentType, String filename) {
        String ct = contentType == null ? "" : contentType.toLowerCase();
        String fn = filename == null ? "" : filename.toLowerCase();
        return ct.contains("wordprocessingml") || fn.endsWith(".docx");
    }
}
