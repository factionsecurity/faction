package com.fuse.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.AssessmentStatsDTO;
import com.fuse.api.dto.VulnerabilityStatsDTO;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.Note;
import com.fuse.dao.Permissions;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Unit tests for the Assessment Stats API endpoint and DTOs.
 * Tests the lightweight assessment stats response that excludes large
 * text blobs (descriptions, recommendations, details) and resolves
 * severity IDs to RiskLevel labels from the backend.
 */
public class AssessmentStatsAPITest {

    private ObjectMapper mapper;
    private User testUser;
    private Assessment testAssessment;
    private AssessmentType testAssessmentType;
    private List<RiskLevel> seededRiskLevels;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        setupTestData();
        seededRiskLevels = createSeededRiskLevels();
    }

    /**
     * Sets the transient levels field on a Vulnerability so that
     * getOverallStr()/getImpactStr()/getLikelyhoodStr() work without a DB.
     */
    private void setRiskLevels(Vulnerability vuln, List<RiskLevel> levels) throws Exception {
        Field f = Vulnerability.class.getDeclaredField("levels");
        f.setAccessible(true);
        f.set(vuln, levels);
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

        testAssessmentType = new AssessmentType();
        testAssessmentType.setId(1L);
        testAssessmentType.setType("Web Application");

        testAssessment = new Assessment();
        testAssessment.setId(100L);
        testAssessment.setName("Test Assessment");
        testAssessment.setAppId("APP-123");
        testAssessment.setType(testAssessmentType);
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
    }

    @Test
    public void testAssessmentStatsDTOBasicFields() {
        AssessmentStatsDTO dto = AssessmentStatsDTO.fromEntity(testAssessment, new ArrayList<>());

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(100L), dto.getId());
        assertEquals("Name should match", "Test Assessment", dto.getName());
        assertEquals("App ID should match", "APP-123", dto.getAppId());
        assertEquals("Type should match", "Web Application", dto.getType());
        assertEquals("Status should match", "In Progress", dto.getStatus());
        assertEquals("Campaign should match", "Q1 Testing", dto.getCampaign());
    }

    @Test
    public void testAssessmentStatsDTOWithContacts() {
        AssessmentStatsDTO dto = AssessmentStatsDTO.fromEntity(testAssessment, new ArrayList<>());

        assertNotNull("Assessors should not be null", dto.getAssessors());
        assertEquals("Should have 1 assessor", 1, dto.getAssessors().size());
        assertEquals("Assessor username should match", "testuser", dto.getAssessors().get(0).getUsername());

        assertNotNull("Engagement contact should not be null", dto.getEngagementContact());
        assertEquals("Engagement user should match", "testuser", dto.getEngagementContact().getUsername());

        assertNotNull("Remediation contact should not be null", dto.getRemediationContact());
        assertEquals("Remediation user should match", "testuser", dto.getRemediationContact().getUsername());
    }

    @Test
    public void testAssessmentStatsDTOWithNullVulns() {
        testAssessment.setVulns(null);
        AssessmentStatsDTO dto = AssessmentStatsDTO.fromEntity(testAssessment, null);

        assertNotNull("DTO should not be null", dto);
        assertNullNull("Vulnerabilities should be null", dto.getVulnerabilities());
        assertNullNull("Risk summary should be null", dto.getRiskSummary());
    }

    @Test
    public void testVulnerabilityStatsFieldsPresent() throws Exception {
        Vulnerability vuln = createTestVulnerability();
        vuln.setOverall(5L);
        vuln.setImpact(4L);
        vuln.setLikelyhood(3L);
        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);

        assertEquals("Name should be present", "SQL Injection", dto.getName());
        assertEquals("Overall should be present", Long.valueOf(5L), dto.getOverall());
        assertEquals("Impact should be present", Long.valueOf(4L), dto.getImpact());
        assertEquals("Likelihood should be present", Long.valueOf(3L), dto.getLikelyhood());
        assertEquals("Tracking should be present", "VID-0001", dto.getTracking());
        assertEquals("CVSS score should be present", "9.1", dto.getCvssScore());
        assertEquals("Created should be present", String.valueOf(vuln.getCreated().getTime()), dto.getCreated());
    }

    @Test
    public void testVulnerabilityStatsRiskLevelsFromBackend() throws Exception {
        Vulnerability vuln = createTestVulnerability();
        vuln.setOverall(5L);
        vuln.setImpact(4L);
        vuln.setLikelyhood(5L);
        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);

        // Verify risk level labels are resolved from RiskLevel table
        // Seeded labels: 0=Informational, 1=Recommended, 2=Low, 3=Medium, 4=High, 5=Critical
        assertEquals("Overall should resolve to Critical (riskId=5)", "Critical", dto.getOverallStr());
        assertEquals("Impact should resolve to High (riskId=4)", "High", dto.getImpactStr());
        assertEquals("Likelihood should resolve to Critical (riskId=5)", "Critical", dto.getLikelyhoodStr());
    }

    @Test
    public void testVulnerabilityStatsWithCategory() throws Exception {
        Vulnerability vuln = createTestVulnerability();

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");
        vuln.setCategory(cat);

        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);

        assertEquals("Category should be present", "Injection", dto.getCategory());
    }

    @Test
    public void testVulnerabilityStatsWithSection() throws Exception {
        Vulnerability vuln = createTestVulnerability();
        // Set section field directly since setSection() is gated by ReportFeatures.allowSections()
        Field sectionField = Vulnerability.class.getDeclaredField("section");
        sectionField.setAccessible(true);
        sectionField.set(vuln, "Cross_Site_Scripting");

        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);

        assertEquals("Section should convert underscores to spaces", "Cross Site Scripting", dto.getSection());
    }

    @Test
    public void testVulnerabilityStatsWithCustomFields() throws Exception {
        Vulnerability vuln = createTestVulnerability();

        List<CustomField> customFields = new ArrayList<>();
        CustomField cf1 = new CustomField();
        cf1.setId(1L);
        CustomType ct1 = new CustomType();
        ct1.setKey("owasp_category");
        ct1.setFieldType(CustomType.FieldType.STRING.getValue());
        cf1.setType(ct1);
        cf1.setValue("A03:2021");
        customFields.add(cf1);
        vuln.setCustomFields(customFields);

        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);
        dto.setCustomFieldsFromEntity(vuln.getCustomFields());

        assertNotNull("Custom fields should not be null", dto.getCustomFields());
        assertEquals("Should have 1 custom field", 1, dto.getCustomFields().size());
        assertEquals("OWASP category should match", "A03:2021", dto.getCustomFields().get(0).getValue());
    }

    @Test
    public void testVulnerabilityStatsUnassignedRiskLevel() throws Exception {
        Vulnerability vuln = createTestVulnerability();
        vuln.setOverall(99L); // Non-existent riskId
        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);

        assertEquals("Unassigned riskId should return Unassigned", "Unassigned", dto.getOverallStr());
    }

    @Test
    public void testRiskSummaryCalculation() throws Exception {
        List<VulnerabilityStatsDTO> vulns = new ArrayList<>();

        Vulnerability v1 = createTestVulnerability();
        v1.setOverall(5L);
        setRiskLevels(v1, seededRiskLevels);
        vulns.add(VulnerabilityStatsDTO.fromEntity(v1));

        Vulnerability v2 = createTestVulnerability();
        v2.setId(2L);
        v2.setOverall(5L);
        setRiskLevels(v2, seededRiskLevels);
        vulns.add(VulnerabilityStatsDTO.fromEntity(v2));

        Vulnerability v3 = createTestVulnerability();
        v3.setId(3L);
        v3.setOverall(4L);
        setRiskLevels(v3, seededRiskLevels);
        vulns.add(VulnerabilityStatsDTO.fromEntity(v3));

        AssessmentStatsDTO asmtDto = AssessmentStatsDTO.fromEntity(testAssessment, vulns);

        assertNotNull("Risk summary should not be null", asmtDto.getRiskSummary());
        Map<String, Integer> summary = asmtDto.getRiskSummary();
        assertEquals("Should have 2 Critical", Integer.valueOf(2), summary.get("Critical"));
        assertEquals("Should have 1 High", Integer.valueOf(1), summary.get("High"));
    }

    @Test
    public void testAssessmentStatsDTOSerialization() throws Exception {
        List<VulnerabilityStatsDTO> vulns = new ArrayList<>();
        Vulnerability v = createTestVulnerability();
        v.setOverall(5L);
        setRiskLevels(v, seededRiskLevels);
        vulns.add(VulnerabilityStatsDTO.fromEntity(v));

        AssessmentStatsDTO dto = AssessmentStatsDTO.fromEntity(testAssessment, vulns);
        String json = mapper.writeValueAsString(dto);

        assertTrue("JSON should contain Id", json.contains("\"Id\":100"));
        assertTrue("JSON should contain Name", json.contains("\"Name\":\"Test Assessment\""));
        assertTrue("JSON should contain Type", json.contains("\"Type\":\"Web Application\""));
        assertTrue("JSON should contain Status", json.contains("\"Status\":\"In Progress\""));
        assertTrue("JSON should contain Campaign", json.contains("\"Campaign\":\"Q1 Testing\""));
        assertTrue("JSON should contain RiskSummary", json.contains("\"RiskSummary\""));
        assertTrue("JSON should contain Critical count", json.contains("\"Critical\":1"));
    }

    @Test
    public void testVulnerabilityStatsDTOSerialization() throws Exception {
        Vulnerability vuln = createTestVulnerability();
        vuln.setOverall(5L);
        vuln.setImpact(4L);
        vuln.setLikelyhood(3L);
        setRiskLevels(vuln, seededRiskLevels);

        VulnerabilityStatsDTO dto = VulnerabilityStatsDTO.fromEntity(vuln);
        String json = mapper.writeValueAsString(dto);

        assertTrue("JSON should contain Id", json.contains("\"Id\""));
        assertTrue("JSON should contain Name", json.contains("\"Name\":\"SQL Injection\""));
        assertTrue("JSON should contain Overall", json.contains("\"Overall\":5"));
        assertTrue("JSON should contain OverallStr", json.contains("\"OverallStr\":\"Critical\""));
        assertTrue("JSON should contain ImpactStr", json.contains("\"ImpactStr\":\"High\""));
        assertTrue("JSON should contain LikelyhoodStr", json.contains("\"LikelyhoodStr\":\"Medium\""));
        assertTrue("JSON should contain Tracking", json.contains("\"Tracking\":\"VID-0001\""));

        // Verify that fields NOT in VulnerabilityStatsDTO are absent
        assertFalse("JSON should NOT contain Description", json.contains("\"Description\""));
        assertFalse("JSON should NOT contain Recommendation", json.contains("\"Recommendation\""));
        assertFalse("JSON should NOT contain Details", json.contains("\"Details\""));
        assertFalse("JSON should NOT contain Desc_notes", json.contains("\"Desc_notes\""));
        assertFalse("JSON should NOT contain Detail_notes", json.contains("\"Detail_notes\""));
        assertFalse("JSON should NOT contain Rec_notes", json.contains("\"Rec_notes\""));
    }

    @Test
    public void testRiskLevelMappingAllSeededValues() {
        assertEquals("riskId 0 should be Informational", "Informational", seededRiskLevels.get(0).getRisk());
        assertEquals("riskId 1 should be Recommended", "Recommended", seededRiskLevels.get(1).getRisk());
        assertEquals("riskId 2 should be Low", "Low", seededRiskLevels.get(2).getRisk());
        assertEquals("riskId 3 should be Medium", "Medium", seededRiskLevels.get(3).getRisk());
        assertEquals("riskId 4 should be High", "High", seededRiskLevels.get(4).getRisk());
        assertEquals("riskId 5 should be Critical", "Critical", seededRiskLevels.get(5).getRisk());
    }

    @Test
    public void testAssessmentStatsWithEmptyVulnerabilities() throws Exception {
        testAssessment.setVulns(new ArrayList<>());
        AssessmentStatsDTO dto = AssessmentStatsDTO.fromEntity(testAssessment, new ArrayList<>());

        assertNotNull("DTO should not be null", dto);
        // When vulns list is empty, DTO leaves vulnerabilities null (existing behavior)
        assertNullNull("Vulnerabilities should be null for empty list", dto.getVulnerabilities());
        assertNullNull("Risk summary should be null for empty list", dto.getRiskSummary());
    }

    // --- Helper methods ---

    private Vulnerability createTestVulnerability() {
        Vulnerability vuln = new Vulnerability();
        vuln.setId(1L);
        vuln.setName("SQL Injection");
        vuln.setAssessmentId(100L);
        vuln.setTracking("VID-0001");
        vuln.setCvssScore("9.1");
        vuln.setCvssString("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H");
        vuln.setCreated(new Date());
        return vuln;
    }

    /**
     * Creates RiskLevel list matching the seeded values from Login.java:
     * 0=Informational, 1=Recommended, 2=Low, 3=Medium, 4=High, 5=Critical
     */
    private List<RiskLevel> createSeededRiskLevels() {
        List<RiskLevel> levels = new ArrayList<>();
        String[] riskLabels = { "Informational", "Recommended", "Low", "Medium", "High", "Critical" };
        for (int i = 0; i < riskLabels.length; i++) {
            RiskLevel rl = new RiskLevel();
            rl.setRiskId(i);
            rl.setRisk(riskLabels[i]);
            levels.add(rl);
        }
        return levels;
    }

    /**
     * JUnit 4 doesn't have assertNull for non-nullable types.
     * This is a workaround for List/Map null checks.
     */
    private void assertNullNull(String message, Object actual) {
        if (actual != null) {
            throw new AssertionError(message + ": expected null but was " + actual);
        }
    }
}
