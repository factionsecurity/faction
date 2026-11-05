package com.fuse.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.VulnerabilityDTO;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Category;
import com.fuse.dao.FinalReport;
import com.fuse.dao.FinalReportVariant;
import com.fuse.dao.Permissions;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Covers the API surface a bulk export or migration depends on: choosing a specific
 * report variant, listing which variants exist, and the vulnerability lifecycle fields
 * (status and its backing dates) that an importer needs to reproduce a finding's state.
 */
public class MigrationExportAPITest {

    private assessments api;
    private User manager;
    private Assessment assessment;

    @Before
    public void setUp() {
        api = new assessments();

        Permissions perms = new Permissions();
        perms.setManager(true);
        perms.setAssessor(true);
        perms.setAdmin(true);

        manager = new User();
        manager.setId(1L);
        manager.setUsername("manager");
        manager.setEmail("manager@example.com");
        manager.setPermissions(perms);

        AssessmentType type = new AssessmentType();
        type.setId(1L);
        type.setType("Web Application");

        assessment = new Assessment();
        assessment.setId(100L);
        assessment.setName("Payments Portal");
        assessment.setAppId("APP-1");
        assessment.setType(type);
        assessment.setStart(new Date());
        assessment.setEnd(new Date());
        assessment.setVulns(new ArrayList<>());
    }

    private static FinalReportVariant variant(String fileType, String content) {
        FinalReportVariant v = new FinalReportVariant();
        v.setFileType(fileType);
        v.setBase64Content(Base64.encodeBase64String(content.getBytes()));
        return v;
    }

    private static FinalReport reportWith(FinalReportVariant... variants) {
        FinalReport report = new FinalReport();
        report.setVariants(new ArrayList<>(Arrays.asList(variants)));
        return report;
    }

    // ── selectVariant ────────────────────────────────────────────────────────

    @Test
    public void selectVariantPrefersPdfWhenNoTypeRequested() {
        FinalReport report = reportWith(variant("docx", "DOCX BODY"), variant("pdf", "PDF BODY"));

        assertEquals("pdf", assessments.selectVariant(report, null).getFileType());
        assertEquals("pdf", assessments.selectVariant(report, "").getFileType());
    }

    @Test
    public void selectVariantHonoursAnExplicitType() {
        FinalReport report = reportWith(variant("docx", "DOCX BODY"), variant("pdf", "PDF BODY"));

        assertEquals("docx", assessments.selectVariant(report, "docx").getFileType());
        assertEquals("docx", assessments.selectVariant(report, "  DOCX  ").getFileType());
    }

    @Test
    public void selectVariantReturnsNullRatherThanASubstituteFormat() {
        // A caller that asked for a DOCX must not be handed a PDF — a migration would
        // file it under the wrong document type.
        FinalReport report = reportWith(variant("pdf", "PDF BODY"));

        assertNull(assessments.selectVariant(report, "docx"));
    }

    @Test
    public void selectVariantFallsBackToTheOnlyVariantWhenThereIsNoPdf() {
        FinalReport report = reportWith(variant("docx", "DOCX BODY"));

        assertEquals("docx", assessments.selectVariant(report, null).getFileType());
    }

    @Test
    public void selectVariantReadsLegacyReportsWithNoVariantList() {
        // Pre-variant reports keep their content on the FinalReport itself.
        FinalReport legacy = new FinalReport();
        legacy.setFileType("pdf");
        legacy.setBase64EncodedPdf(Base64.encodeBase64String("LEGACY".getBytes()));

        assertEquals("pdf", assessments.selectVariant(legacy, null).getFileType());
        assertEquals("pdf", assessments.selectVariant(legacy, "pdf").getFileType());
        assertNull(assessments.selectVariant(legacy, "docx"));
    }

    // ── variantTypes ─────────────────────────────────────────────────────────

    @Test
    public void variantTypesListsEveryStoredFormat() {
        JSONArray types = assessments.variantTypes(
                reportWith(variant("docx", "DOCX BODY"), variant("pdf", "PDF BODY")));

        assertEquals(2, types.size());
        assertTrue(types.contains("docx"));
        assertTrue(types.contains("pdf"));
    }

    @Test
    public void variantTypesIsEmptyForAMissingReport() {
        assertTrue(assessments.variantTypes(null).isEmpty());
    }

    @Test
    public void variantTypesSkipsVariantsWithNoContent() {
        // Advertising an empty variant would send the caller after a download that can only 404.
        FinalReportVariant empty = new FinalReportVariant();
        empty.setFileType("pdf");

        JSONArray types = assessments.variantTypes(reportWith(variant("docx", "DOCX BODY"), empty));

        assertEquals(1, types.size());
        assertTrue(types.contains("docx"));
    }

    // ── downloadReport ───────────────────────────────────────────────────────

    @Test
    public void downloadReturnsTheRequestedVariantWithItsOwnContentType() {
        assessment.setFinalReport(reportWith(variant("docx", "DOCX BODY"), variant("pdf", "PDF BODY")));

        Response docx = api.invokeDownloadReport(manager, assessment, 100L, "docx", false);
        assertEquals(200, docx.getStatus());
        assertEquals("DOCX BODY", new String((byte[]) docx.getEntity()));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docx.getMediaType().toString());

        Response pdf = api.invokeDownloadReport(manager, assessment, 100L, "pdf", false);
        assertEquals(200, pdf.getStatus());
        assertEquals("PDF BODY", new String((byte[]) pdf.getEntity()));
        assertEquals("application/pdf", pdf.getMediaType().toString());
    }

    @Test
    public void downloadWithNoTypeStillPrefersThePdf() {
        assessment.setFinalReport(reportWith(variant("docx", "DOCX BODY"), variant("pdf", "PDF BODY")));

        Response response = api.invokeDownloadReport(manager, assessment, 100L);

        assertEquals(200, response.getStatus());
        assertEquals("PDF BODY", new String((byte[]) response.getEntity()));
    }

    @Test
    public void downloadReturnsTheRetestReportWhenAsked() {
        assessment.setFinalReport(reportWith(variant("pdf", "FINAL BODY")));
        assessment.setRetestReport(reportWith(variant("pdf", "RETEST BODY")));

        Response response = api.invokeDownloadReport(manager, assessment, 100L, "pdf", true);

        assertEquals(200, response.getStatus());
        assertEquals("RETEST BODY", new String((byte[]) response.getEntity()));
        String disposition = response.getHeaderString("Content-Disposition");
        assertTrue(disposition, disposition.contains("Retest Report.pdf"));
    }

    @Test
    public void downloadIs404WhenTheRequestedTypeIsAbsent() {
        assessment.setFinalReport(reportWith(variant("pdf", "PDF BODY")));

        Response response = api.invokeDownloadReport(manager, assessment, 100L, "docx", false);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void downloadIs404WhenThereIsNoRetestReport() {
        assessment.setFinalReport(reportWith(variant("pdf", "PDF BODY")));

        Response response = api.invokeDownloadReport(manager, assessment, 100L, null, true);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void downloadSurvivesAnAssessmentWithNoType() {
        // Drafts imported from elsewhere can be missing a type; the filename should degrade,
        // not throw.
        assessment.setType(null);
        assessment.setFinalReport(reportWith(variant("pdf", "PDF BODY")));

        Response response = api.invokeDownloadReport(manager, assessment, 100L, "pdf", false);

        assertEquals(200, response.getStatus());
        assertTrue(response.getHeaderString("Content-Disposition").contains("Payments Portal - Report.pdf"));
    }

    // ── VulnerabilityDTO lifecycle fields ────────────────────────────────────

    @Test
    public void vulnerabilityDtoCarriesStatusDatesAndCategory() throws Exception {
        Category category = new Category();
        category.setId(7L);
        category.setName("Injection");

        Date opened = new Date(1_700_000_000_000L);
        Date closed = new Date(1_700_086_400_000L);
        Date devClosed = new Date(1_700_003_600_000L);
        Date stagingClosed = new Date(1_700_007_200_000L);

        Vulnerability vuln = new Vulnerability();
        vuln.setId(42L);
        vuln.setName("SQL Injection");
        vuln.setOverall(5L);
        vuln.setImpact(5L);
        vuln.setLikelyhood(4L);
        vuln.setStatus(Vulnerability.StatusClosed);
        vuln.setOpened(opened);
        vuln.setClosed(closed);
        vuln.setDevClosed(devClosed);
        vuln.setStagingClosed(stagingClosed);
        vuln.setCategory(category);
        vuln.setAssessmentId(100L);
        vuln.setLevels(riskLevels());

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);

        assertEquals("Closed", dto.getStatus());
        assertEquals(String.valueOf(opened.getTime()), dto.getOpened());
        assertEquals(String.valueOf(closed.getTime()), dto.getClosed());
        assertEquals(String.valueOf(devClosed.getTime()), dto.getDevClosed());
        assertEquals(String.valueOf(stagingClosed.getTime()), dto.getStagingClosed());
        assertEquals("Injection", dto.getCategory());
        assertEquals(Long.valueOf(7L), dto.getCategoryId());
        assertEquals(Long.valueOf(100L), dto.getAssessmentId());

        // The names are what an importer maps against, so they must survive serialization.
        String json = new ObjectMapper().writeValueAsString(dto);
        assertTrue(json, json.contains("\"Status\":\"Closed\""));
        assertTrue(json, json.contains("\"Category\":\"Injection\""));
        assertTrue(json, json.contains("\"Opened\":\"" + opened.getTime() + "\""));
    }

    @Test
    public void vulnerabilityDtoOmitsLifecycleFieldsThatAreNotSet() throws Exception {
        Vulnerability vuln = new Vulnerability();
        vuln.setId(43L);
        vuln.setName("Open Finding");
        vuln.setOverall(3L);
        vuln.setStatus(Vulnerability.StatusOpen);
        vuln.setOpened(new Date(1_700_000_000_000L));
        vuln.setLevels(riskLevels());

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);

        assertEquals("Open", dto.getStatus());
        assertNotNull(dto.getOpened());
        assertNull(dto.getClosed());
        assertNull(dto.getCategory());
        assertNull(dto.getCategoryId());
        // Never attached to an assessment, so the field stays out of the payload
        // rather than unboxing a null or reporting assessment 0.
        assertNull(dto.getAssessmentId());

        String json = new ObjectMapper().writeValueAsString(dto);
        assertFalse(json, json.contains("\"Closed\""));
        assertFalse(json, json.contains("\"Category\""));
        assertFalse(json, json.contains("\"AssessmentId\""));
    }

    private static List<RiskLevel> riskLevels() {
        List<RiskLevel> levels = new ArrayList<>();
        levels.add(level(5, "Critical"));
        levels.add(level(4, "High"));
        levels.add(level(3, "Medium"));
        return levels;
    }

    private static RiskLevel level(int riskId, String name) {
        RiskLevel level = new RiskLevel();
        level.setRiskId(riskId);
        level.setRisk(name);
        return level;
    }
}
