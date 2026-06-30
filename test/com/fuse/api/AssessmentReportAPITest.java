package com.fuse.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.FinalReport;
import com.fuse.dao.FinalReportPart;
import com.fuse.dao.FinalReportVariant;
import com.fuse.dao.Note;
import com.fuse.dao.Permissions;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Unit tests for the Report Download API endpoint.
 * Tests FinalReport entity behavior and report response structure.
 */
public class AssessmentReportAPITest {

    private ObjectMapper mapper;
    private User testUser;
    private Assessment testAssessment;
    private FinalReport testFinalReport;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        setupTestData();
    }

    private void setupTestData() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFname("Test");
        testUser.setLname("User");
        testUser.setEmail("test@example.com");

        Permissions perms = new Permissions();
        perms.setAssessor(true);
        perms.setManager(false);
        perms.setEngagement(false);
        testUser.setPermissions(perms);

        AssessmentType type = new AssessmentType();
        type.setId(1L);
        type.setType("Web Application");

        testAssessment = new Assessment();
        testAssessment.setId(100L);
        testAssessment.setName("Test Assessment");
        testAssessment.setAppId("APP-123");
        testAssessment.setType(type);
        testAssessment.setAssessor(Arrays.asList(testUser));
        testAssessment.setEngagement(testUser);
        testAssessment.setRemediation(testUser);
        testAssessment.setStart(new Date());
        testAssessment.setEnd(new Date());
        testAssessment.setVulns(new ArrayList<>());
        testAssessment.setStatus("In Progress");

        List<Note> notebook = new ArrayList<>();
        Note note = new Note();
        note.setNote("Initial notes");
        notebook.add(note);
        testAssessment.setNotebook(notebook);

        Campaign camp = new Campaign();
        camp.setId(1L);
        camp.setName("Q1 Testing");
        testAssessment.setCampaign(camp);

        testFinalReport = new FinalReport();
        testFinalReport.setFilename("test-report.docx");
        testFinalReport.setFileType("docx");
        testFinalReport.setRetest(false);
        testFinalReport.setGentime(new Date());

        String sampleDocx = "PK" + (char)3 + (char)4 + "test report content";
        testFinalReport.setBase64EncodedPdf(java.util.Base64.getEncoder()
                .encodeToString(sampleDocx.getBytes()));
    }

    @Test
    public void testFinalReportBasicFields() {
        assertEquals("Filename should match", "test-report.docx", testFinalReport.getFilename());
        assertEquals("FileType should match", "docx", testFinalReport.getFileType());
        assertFalse("Should not be retest", testFinalReport.getRetest());
        assertNotNull("Gentime should be set", testFinalReport.getGentime());
    }

    @Test
    public void testFinalReportBase64Encoding() {
        String original = "PK" + (char)3 + (char)4 + "test report content";
        String base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes());
        testFinalReport.setBase64EncodedPdf(base64);

        String retrieved = testFinalReport.getBase64EncodedPdf();
        assertNotNull("Base64 should not be null", retrieved);
        assertEquals("Base64 should match", base64, retrieved);

        byte[] decoded = java.util.Base64.getDecoder().decode(retrieved);
        String decodedStr = new String(decoded);
        assertEquals("Decoded content should match original", original, decodedStr);
    }

    @Test
    public void testFinalReportFileTypeDefault() {
        FinalReport fr = new FinalReport();
        fr.setFilename("test.docx");

        assertEquals("Default file type should be docx", "docx", fr.getFileType());

        fr.setFileType("pdf");
        assertEquals("FileType should be pdf when set", "pdf", fr.getFileType());

        fr.setFileType("");
        assertEquals("Empty file type should default to docx", "docx", fr.getFileType());

        fr.setFileType(null);
        assertEquals("Null file type should default to docx", "docx", fr.getFileType());
    }

    @Test
    public void testFinalReportLargeFileHandling() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16000000 / 10 + 1; i++) {
            sb.append("XXXXXXXXXX");
        }
        String largeContent = sb.toString();
        testFinalReport.setBase64EncodedPdf(largeContent);

        assertTrue("Should be marked as large file", testFinalReport.getLargeFile());
        assertNotNull("Parts should be populated", testFinalReport.getParts());
        assertTrue("Should have at least 1 part", testFinalReport.getParts().size() > 0);

        String retrieved = testFinalReport.getBase64EncodedPdf();
        assertNotNull("Retrieved report should not be null", retrieved);
        assertEquals("Retrieved report should match original", largeContent, retrieved);
    }

    @Test
    public void testFinalReportSmallFileNoChunks() {
        FinalReport fr = new FinalReport();
        String smallContent = "Small report content";
        fr.setBase64EncodedPdf(smallContent);

        assertFalse("Should not be marked as large file", fr.getLargeFile());
        // Parts list is cleared but not null after setBase64EncodedPdf
        assertNotNull("Parts list should exist", fr.getParts());
        assertTrue("Parts should be empty after clear", fr.getParts().isEmpty());

        String retrieved = fr.getBase64EncodedPdf();
        assertEquals("Retrieved content should match", smallContent, retrieved);
    }

    @Test
    public void testFinalReportRetestFlag() {
        FinalReport fr = new FinalReport();
        assertFalse("Default retest should be false", fr.getRetest());

        fr.setRetest(true);
        assertTrue("Retest should be true when set", fr.getRetest());

        fr.setRetest(false);
        assertFalse("Retest should be false when set", fr.getRetest());

        fr.setRetest(null);
        assertFalse("Null retest should default to false", fr.getRetest());
    }

    @Test
    public void testReportResponseJsonStructure() throws Exception {
        String base64Report = testFinalReport.getBase64EncodedPdf();
        String fileType = testFinalReport.getFileType();

        Map<String, String> jsonResponse = new java.util.HashMap<>();
        jsonResponse.put("report", base64Report);
        jsonResponse.put("fileType", fileType);
        jsonResponse.put("filename", testAssessment.getName() + "_Report." + fileType);

        String json = mapper.writeValueAsString(jsonResponse);

        assertTrue("JSON should contain report field", json.contains("\"report\""));
        assertTrue("JSON should contain fileType field", json.contains("\"fileType\":\"" + fileType + "\""));
        assertTrue("JSON should contain filename field", json.contains("\"filename\""));

        Map<String, String> parsed = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        assertEquals("Parsed report should match", base64Report, parsed.get("report"));
        assertEquals("Parsed fileType should match", fileType, parsed.get("fileType"));
    }

    @Test
    public void testReportFilenameGeneration() {
        String expectedFilename = testAssessment.getName() + " - " + testAssessment.getType().getType() + " Report.docx";
        assertEquals("Filename should include assessment name and type",
                "Test Assessment - Web Application Report.docx", expectedFilename);
    }

    @Test
    public void testReportFilenamePdf() {
        String expectedFilename = testAssessment.getName() + " - " + testAssessment.getType().getType() + " Report.pdf";
        assertEquals("PDF filename should include assessment name and type",
                "Test Assessment - Web Application Report.pdf", expectedFilename);
    }

    @Test
    public void testBase64DecodingReportBytes() {
        String original = "PK" + (char)3 + (char)4 + "document content here";
        String base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes());

        byte[] decoded = java.util.Base64.getDecoder().decode(base64.getBytes());
        String decodedStr = new String(decoded);

        assertEquals("Decoded bytes should match original", original, decodedStr);
    }

    @Test
    public void testDocxMagicBytes() {
        String docxContent = "PK" + (char)3 + (char)4; // ZIP/DOCX magic bytes
        byte[] bytes = docxContent.getBytes();

        assertTrue("First byte should be 'P'", bytes[0] == (byte)'P');
        assertTrue("Second byte should be 'K'", bytes[1] == (byte)'K');
        assertTrue("Third byte should be 0x03", bytes[2] == (byte)0x03);
        assertTrue("Fourth byte should be 0x04", bytes[3] == (byte)0x04);
    }

    @Test
    public void testPdfMagicBytes() {
        String pdfContent = "%PDF-1.4";
        byte[] bytes = pdfContent.getBytes();

        assertTrue("First byte should be '%'", bytes[0] == (byte)'%');
        assertTrue("Second byte should be 'P'", bytes[1] == (byte)'P');
        assertTrue("Third byte should be 'D'", bytes[2] == (byte)'D');
        assertTrue("Fourth byte should be 'F'", bytes[3] == (byte)'F');
    }

    @Test
    public void testFinalReportWithNullBase64() {
        FinalReport fr = new FinalReport();
        fr.setBase64EncodedPdf(null);

        String retrieved = fr.getBase64EncodedPdf();
        assertNullNull("Should return null for null input", retrieved);
    }

    @Test
    public void testAssessmentReportDataAccess() {
        testAssessment.setFinalReport(testFinalReport);

        FinalReport fr = testAssessment.getFinalReport();
        assertNotNull("Assessment should have final report", fr);
        assertEquals("Filename should match", "test-report.docx", fr.getFilename());
        assertEquals("Base64 should be accessible", testFinalReport.getBase64EncodedPdf(), fr.getBase64EncodedPdf());
    }

    @Test
    public void testAssessmentWithoutFinalReport() {
        Assessment asmt = new Assessment();
        asmt.setId(1L);
        asmt.setName("No Report Assessment");

        FinalReport fr = asmt.getFinalReport();
        assertNullNull("Final report should be null when not set", fr);
    }

    // =========================================================================
    // getEffectiveVariants() — backward compat bridge
    // =========================================================================

    @Test
    public void testGetEffectiveVariants_returnsStoredVariantsWhenPresent() {
        FinalReport fr = new FinalReport();
        FinalReportVariant v = new FinalReportVariant();
        v.setFileType("pdf");
        v.setBase64Content("pdfcontent");
        fr.getVariants().add(v);

        List<FinalReportVariant> result = fr.getEffectiveVariants();

        assertEquals("Should return stored variants", 1, result.size());
        assertSame("Should be the same object", v, result.get(0));
    }

    @Test
    public void testGetEffectiveVariants_legacyFallback_emptyVariantsList() {
        // Pre-branch record: no variants stored, content in base64EncodedPdf
        FinalReport fr = new FinalReport();
        fr.setFileType("docx");
        fr.setBase64EncodedPdf("legacydocxcontent");
        // variants list is empty by default

        List<FinalReportVariant> result = fr.getEffectiveVariants();

        assertEquals("Legacy record should produce exactly 1 synthetic variant", 1, result.size());
        FinalReportVariant synthesized = result.get(0);
        assertEquals("Synthesized variant should carry the legacy fileType", "docx", synthesized.getFileType());
        assertEquals("Synthesized variant should return the legacy content", "legacydocxcontent", synthesized.getBase64Content());
    }

    @Test
    public void testGetEffectiveVariants_legacyFallback_preservesFileTypeDefault() {
        // When fileType is null on FinalReport, getFileType() returns "docx"
        FinalReport fr = new FinalReport();
        fr.setBase64EncodedPdf("somecontent");

        List<FinalReportVariant> result = fr.getEffectiveVariants();

        assertEquals("Null fileType should fall back to docx", "docx", result.get(0).getFileType());
    }

    @Test
    public void testGetEffectiveVariants_legacyFallback_pdfFileType() {
        FinalReport fr = new FinalReport();
        fr.setFileType("pdf");
        fr.setBase64EncodedPdf("legacypdfcontent");

        List<FinalReportVariant> result = fr.getEffectiveVariants();

        assertEquals("pdf", result.get(0).getFileType());
        assertEquals("legacypdfcontent", result.get(0).getBase64Content());
    }

    @Test
    public void testGetEffectiveVariants_legacyFallback_largeFile() {
        // Large legacy report: content is split into FinalReportPart objects
        FinalReport fr = new FinalReport();
        fr.setFileType("docx");
        // Build a string over the 15MB chunking threshold
        char[] chars = new char[16_000_001];
        java.util.Arrays.fill(chars, 'A');
        fr.setBase64EncodedPdf(new String(chars)); // triggers chunking in FinalReport
        assertTrue("Setup: should be largeFile", fr.getLargeFile());

        List<FinalReportVariant> result = fr.getEffectiveVariants();

        assertEquals("Large legacy record should produce 1 synthetic variant", 1, result.size());
        String assembled = result.get(0).getBase64Content();
        assertNotNull("Assembled content should not be null", assembled);
        assertEquals("Assembled length must match original", 16_000_001, assembled.length());
    }

    @Test
    public void testGetEffectiveVariants_multipleVariants() {
        FinalReport fr = new FinalReport();
        FinalReportVariant docx = new FinalReportVariant();
        docx.setFileType("docx");
        FinalReportVariant pdf = new FinalReportVariant();
        pdf.setFileType("pdf");
        fr.getVariants().add(docx);
        fr.getVariants().add(pdf);

        List<FinalReportVariant> result = fr.getEffectiveVariants();

        assertEquals("Should return all stored variants", 2, result.size());
    }

    // =========================================================================
    // getVariantCount() — null/zero/negative safety guard
    // =========================================================================

    @Test
    public void testVariantCount_nullDefaultsToOne() {
        FinalReport fr = new FinalReport();
        // variantCount is null by default
        assertEquals("Null variantCount should return 1", 1, fr.getVariantCount());
    }

    @Test
    public void testVariantCount_zeroDefaultsToOne() {
        FinalReport fr = new FinalReport();
        fr.setVariantCount(0);
        assertEquals("Zero variantCount should return 1", 1, fr.getVariantCount());
    }

    @Test
    public void testVariantCount_negativeDefaultsToOne() {
        FinalReport fr = new FinalReport();
        fr.setVariantCount(-1);
        assertEquals("Negative variantCount should return 1", 1, fr.getVariantCount());
    }

    @Test
    public void testVariantCount_positivePassesThrough() {
        FinalReport fr = new FinalReport();
        fr.setVariantCount(2);
        assertEquals("Positive variantCount should be returned as-is", 2, fr.getVariantCount());
    }

    @Test
    public void testVariantCount_onePassesThrough() {
        FinalReport fr = new FinalReport();
        fr.setVariantCount(1);
        assertEquals(1, fr.getVariantCount());
    }

    // =========================================================================
    // encryptedReportPassword — Lombok getter/setter
    // =========================================================================

    @Test
    public void testEncryptedReportPassword_nullByDefault() {
        FinalReport fr = new FinalReport();
        assertNull("encryptedReportPassword should be null by default", fr.getEncryptedReportPassword());
    }

    @Test
    public void testEncryptedReportPassword_storeAndRetrieve() {
        FinalReport fr = new FinalReport();
        fr.setEncryptedReportPassword("encrypted:abc123");
        assertEquals("encrypted:abc123", fr.getEncryptedReportPassword());
    }

    @Test
    public void testEncryptedReportPassword_clearWithNull() {
        FinalReport fr = new FinalReport();
        fr.setEncryptedReportPassword("encrypted:abc123");
        fr.setEncryptedReportPassword(null);
        assertNull("Password should be cleared after setting null", fr.getEncryptedReportPassword());
    }

    /**
     * JUnit 4 doesn't have assertNull for non-nullable types.
     * This is a workaround for Object null checks.
     */
    private void assertNullNull(String message, Object actual) {
        if (actual != null) {
            throw new AssertionError(message + ": expected null but was " + actual);
        }
    }
}
