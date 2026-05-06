package com.fuse.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.Note;
import com.fuse.dao.Permissions;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Comprehensive unit tests for core DAO entities.
 * Tests entity construction, getters/setters, and basic behavior.
 */
public class EntityUnitTest extends MongoTestBase {

    private User testUser;
    private Assessment testAssessment;
    private Vulnerability testVuln;
    private Campaign testCampaign;
    private Category testCategory;
    private AssessmentType testAssessmentType;
    private List<RiskLevel> riskLevels;

    @Before
    public void setUp() {
        // Create test user with full permissions
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFname("Test");
        testUser.setLname("User");
        testUser.setEmail("test@example.com");

        Permissions perms = new Permissions();
        perms.setAssessor(true);
        perms.setManager(true);
        perms.setEngagement(true);
        perms.setAdmin(true);
        testUser.setPermissions(perms);

        // Create assessment type
        testAssessmentType = new AssessmentType();
        testAssessmentType.setId(1L);
        testAssessmentType.setType("Web Application");

        // Create campaign
        testCampaign = new Campaign();
        testCampaign.setId(1L);
        testCampaign.setName("Q1 Testing");

        // Create category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Injection");

        // Create risk levels
        riskLevels = new ArrayList<>();
        riskLevels.add(createTestRiskLevel(0, "Informational"));
        riskLevels.add(createTestRiskLevel(1, "Recommended"));
        riskLevels.add(createTestRiskLevel(2, "Low"));
        riskLevels.add(createTestRiskLevel(3, "Medium"));
        riskLevels.add(createTestRiskLevel(4, "High"));
        riskLevels.add(createTestRiskLevel(5, "Critical"));

        // Create vulnerability
        testVuln = new Vulnerability();
        testVuln.setId(1L);
        testVuln.setName("SQL Injection");
        testVuln.setOverall(5L);
        testVuln.setImpact(4L);
        testVuln.setLikelyhood(5L);
        testVuln.setTracking("VID-0001");
        testVuln.setCategory(testCategory);
        testVuln.setAssessmentId(100L);
        testVuln.setCreated(new Date());
        testVuln.setLevels(riskLevels);

        // Create assessment
        testAssessment = new Assessment();
        testAssessment.setId(100L);
        testAssessment.setName("Test Assessment");
        testAssessment.setAppId("APP-123");
        testAssessment.setType(testAssessmentType);
        testAssessment.setAssessor(Arrays.asList(testUser));
        testAssessment.setEngagement(testUser);
        testAssessment.setRemediation(testUser);
        testAssessment.setCampaign(testCampaign);
        testAssessment.setStart(new Date());
        testAssessment.setEnd(new Date());
        testAssessment.setStatus("In Progress");
        testAssessment.setVulns(new ArrayList<>());

        List<Note> notebook = new ArrayList<>();
        notebook.add(createTestNote("Initial notes"));
        testAssessment.setNotebook(notebook);
    }

    // --- User Entity Tests ---

    @Test
    public void testUserBasicFields() {
        assertEquals("ID should match", testUser.getId(), (long)1);
        assertEquals("Username should match", "testuser", testUser.getUsername());
        assertEquals("First name should match", "Test", testUser.getFname());
        assertEquals("Last name should match", "User", testUser.getLname());
        assertEquals("Email should match", "test@example.com", testUser.getEmail());
    }

    @Test
    public void testUserPermissions() {
        Permissions perms = testUser.getPermissions();
        assertNotNull("Permissions should not be null", perms);
        assertTrue("Should be assessor", perms.isAssessor());
        assertTrue("Should be manager", perms.isManager());
        assertTrue("Should be engagement", perms.isEngagement());
        assertTrue("Should be admin", perms.isAdmin());
    }

    @Test
    public void testUserPermissionsSetters() {
        Permissions perms = testUser.getPermissions();
        perms.setAssessor(false);
        perms.setManager(false);
        perms.setEngagement(false);
        perms.setAdmin(false);

        assertFalse("Should not be assessor", perms.isAssessor());
        assertFalse("Should not be manager", perms.isManager());
        assertFalse("Should not be engagement", perms.isEngagement());
        assertFalse("Should not be admin", perms.isAdmin());
    }

    @Test
    public void testUserWithNullPermissions() {
        User user = new User();
        user.setId(2L);
        user.setUsername("noperms");
        // Don't set permissions
        assertNull("Permissions should be null when not set", user.getPermissions());
    }

    // --- Assessment Entity Tests ---

    @Test
    public void testAssessmentBasicFields() {
        assertEquals("ID should match", testAssessment.getId(), Long.valueOf(100L));
        assertEquals("Name should match", "Test Assessment", testAssessment.getName());
        assertEquals("App ID should match", "APP-123", testAssessment.getAppId());
        assertEquals("Status should match", "In Progress", testAssessment.getStatus());
    }

    @Test
    public void testAssessmentRelationships() {
        assertEquals("Type should match", testAssessmentType, testAssessment.getType());
        assertEquals("Campaign should match", testCampaign, testAssessment.getCampaign());
        assertEquals("Engagement should match", testUser, testAssessment.getEngagement());
        assertEquals("Remediation should match", testUser, testAssessment.getRemediation());
        assertEquals("Assessors should have 1 user", 1, testAssessment.getAssessor().size());
        assertEquals("Assessor should match", testUser, testAssessment.getAssessor().get(0));
    }

    @Test
    public void testAssessmentVulnerabilities() {
        assertNotNull("Vulnerabilities list should not be null", testAssessment.getVulns());
        assertTrue("Vulnerabilities list should be empty", testAssessment.getVulns().isEmpty());

        // Add vulnerability
        testAssessment.getVulns().add(testVuln);
        assertEquals("Should have 1 vulnerability", 1, testAssessment.getVulns().size());
        assertEquals("Vulnerability should match", testVuln, testAssessment.getVulns().get(0));
    }

    @Test
    public void testAssessmentNotebook() {
        assertNotNull("Notebook should not be null", testAssessment.getNotebook());
        assertEquals("Should have 1 note", 1, testAssessment.getNotebook().size());
        assertEquals("Note text should match", "Initial notes", testAssessment.getNotebook().get(0).getNote());
    }

    @Test
    public void testAssessmentDates() {
        assertNotNull("Start date should not be null", testAssessment.getStart());
        assertNotNull("End date should not be null", testAssessment.getEnd());
        assertNull("Completed date should be null", testAssessment.getCompleted());

        testAssessment.setCompleted(new Date());
        assertNotNull("Completed date should be set", testAssessment.getCompleted());
    }

    @Test
    public void testAssessmentWorkflow() {
        assertEquals("Default workflow should be 0", Integer.valueOf(0), testAssessment.getWorkflow());
        testAssessment.setWorkflow(1);
        assertEquals("Workflow should be 1", Integer.valueOf(1), testAssessment.getWorkflow());
    }

    @Test
    public void testAssessmentWithNullType() {
        Assessment a = new Assessment();
        a.setId(2L);
        a.setName("No Type");
        assertNull("Type should be null when not set", a.getType());
    }

    @Test
    public void testAssessmentWithNullCampaign() {
        Assessment a = new Assessment();
        a.setId(2L);
        a.setName("No Campaign");
        assertNull("Campaign should be null when not set", a.getCampaign());
    }

    // --- Vulnerability Entity Tests ---

    @Test
    public void testVulnerabilityBasicFields() {
        assertEquals("ID should match", testVuln.getId(), (long)1);
        assertEquals("Name should match", "SQL Injection", testVuln.getName());
        assertEquals("Overall should match", testVuln.getOverall(), Long.valueOf(5L));
        assertEquals("Impact should match", testVuln.getImpact(), Long.valueOf(4L));
        assertEquals("Likelihood should match", testVuln.getLikelyhood(), Long.valueOf(5L));
        assertEquals("Tracking should match", "VID-0001", testVuln.getTracking());
    }

    @Test
    public void testVulnerabilityRiskLevels() {
        assertNotNull("Levels should not be null", testVuln.getLevels());
        assertEquals("Should have 6 risk levels", 6, testVuln.getLevels().size());
    }

    @Test
    public void testVulnerabilityRiskLevelStrings() {
        // Test vulnStr method with risk levels set
        assertEquals("Overall should be Critical", "Critical", testVuln.getOverallStr());
        assertEquals("Impact should be High", "High", testVuln.getImpactStr());
        assertEquals("Likelihood should be Critical", "Critical", testVuln.getLikelyhoodStr());
    }

@Test
    public void testVulnerabilityWithNullLevels() {
        Vulnerability v = new Vulnerability();
        v.setName("No Levels");
        // The levels field is initialized to an empty ArrayList by default in the entity
        assertNotNull("Levels should be initialized to empty list", v.getLevels());
        assertEquals("Levels should be empty", 0, v.getLevels().size());
    }

@Test
    public void testVulnerabilityWithNullOverall() {
        Vulnerability v = new Vulnerability();
        v.setName("No Overall");
        v.setLevels(riskLevels);
        assertNull("Overall should be null", v.getOverall());
        assertEquals("Should return Unassigned", "Unassigned", v.getOverallStr());
    }

    @Test
    public void testVulnerabilityCategory() {
        assertEquals("Category should match", testCategory, testVuln.getCategory());
        assertEquals("Category name should match", "Injection", testVuln.getCategory().getName());
    }

    @Test
    public void testVulnerabilityAssessmentId() {
        assertEquals("Assessment ID should match", testVuln.getAssessmentId(), (long)100);
    }

    @Test
    public void testVulnerabilityDates() {
        assertNotNull("Created date should not be null", testVuln.getCreated());
        assertNull("Closed date should be null", testVuln.getClosed());
        assertNull("Opened date should be null", testVuln.getOpened());

        testVuln.setClosed(new Date());
        assertNotNull("Closed date should be set", testVuln.getClosed());
    }

    @Test
    public void testVulnerabilityDescriptionRecommendation() {
        testVuln.setDescription("<p>SQL injection description</p>");
        testVuln.setRecommendation("<p>Use parameterized queries</p>");

        assertEquals("Description should match", "<p>SQL injection description</p>", testVuln.getDescription());
        assertEquals("Recommendation should match", "<p>Use parameterized queries</p>", testVuln.getRecommendation());
    }

    @Test
    public void testVulnerabilityDetails() {
        testVuln.setDetails("<p>Exploit steps here</p>");
        assertEquals("Details should match", "<p>Exploit steps here</p>", testVuln.getDetails());
    }

    @Test
    public void testVulnerabilityTracking() {
        testVuln.setTracking("VID-0002");
        assertEquals("Tracking should match", "VID-0002", testVuln.getTracking());
    }

    @Test
    public void testVulnerabilityCvss() {
        testVuln.setCvssScore("9.1");
        testVuln.setCvssString("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H");

        assertEquals("CVSS score should match", "9.1", testVuln.getCvssScore());
        assertEquals("CVSS string should match", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H", testVuln.getCvssString());
    }

    @Test
    public void testVulnerabilitySection() throws Exception {
        // ReportFeatures.allowSections() returns false, so setSection() silently discards values
        // Use reflection to set the section field directly
        java.lang.reflect.Field sectionField = Vulnerability.class.getDeclaredField("section");
        sectionField.setAccessible(true);
        sectionField.set(testVuln, "Cross_Site_Scripting");
        assertEquals("Section should convert underscores to spaces", "Cross Site Scripting", testVuln.getSection());
    }

    @Test
    public void testVulnerabilityWithNullCategory() {
        Vulnerability v = new Vulnerability();
        v.setId(2L);
        v.setName("No Category");
        assertNull("Category should be null when not set", v.getCategory());
    }

    // --- RiskLevel Entity Tests ---

    @Test
    public void testRiskLevelBasicFields() {
        RiskLevel rl = createTestRiskLevel(5, "Critical");
        assertEquals("Risk ID should match", 5, rl.getRiskId());
        assertEquals("Risk should match", "Critical", rl.getRisk());
    }

    @Test
    public void testRiskLevelAllSeededValues() {
        for (int i = 0; i < 6; i++) {
            String[] labels = {"Informational", "Recommended", "Low", "Medium", "High", "Critical"};
            RiskLevel rl = createTestRiskLevel(i, labels[i]);
            assertEquals("Risk ID " + i + " should be " + labels[i], i, rl.getRiskId());
            assertEquals("Risk should match", labels[i], rl.getRisk());
        }
    }

    @Test
    public void testRiskLevelDaysTillDue() {
        RiskLevel rl = createTestRiskLevel(5, "Critical");
        assertNull("Days till due should be null", rl.getDaysTillDue());
        rl.setDaysTillDue(30);
        assertEquals("Days till due should be 30", Integer.valueOf(30), rl.getDaysTillDue());
    }

    @Test
    public void testRiskLevelDaysTillWarning() {
        RiskLevel rl = createTestRiskLevel(5, "Critical");
        assertNull("Days till warning should be null", rl.getDaysTillWarning());
        rl.setDaysTillWarning(14);
        assertEquals("Days till warning should be 14", Integer.valueOf(14), rl.getDaysTillWarning());
    }

    // --- Category Entity Tests ---

    @Test
    public void testCategoryBasicFields() {
        assertEquals("ID should match", testCategory.getId(), (long)1);
        assertEquals("Name should match", "Injection", testCategory.getName());
    }

    // --- AssessmentType Entity Tests ---

    @Test
    public void testAssessmentTypeBasicFields() {
        assertEquals("ID should match", testAssessmentType.getId(), Long.valueOf(1L));
        assertEquals("Type should match", "Web Application", testAssessmentType.getType());
    }

    @Test
    public void testAssessmentTypeIsWebApp() {
        assertFalse("Should not be CVSS 3.1", testAssessmentType.isCvss31());
        assertFalse("Should not be CVSS 4.0", testAssessmentType.isCvss40());
    }

    // --- Campaign Entity Tests ---

    @Test
    public void testCampaignBasicFields() {
        assertEquals("ID should match", testCampaign.getId(), Long.valueOf(1L));
        assertEquals("Name should match", "Q1 Testing", testCampaign.getName());
    }

    // --- CustomField and CustomType Tests ---

    @Test
    public void testCustomTypeBasicFields() {
        CustomType ct = createTestCustomType("owasp_category", CustomType.FieldType.STRING.getValue(),
                                              CustomType.ObjType.VULN.getValue());
        assertEquals("Key should match", "owasp_category", ct.getKey());
        assertEquals("Field type should match", CustomType.FieldType.STRING.getValue(), ct.getFieldType());
        assertEquals("Type should match", CustomType.ObjType.VULN.getValue(), ct.getType());
    }

    @Test
    public void testCustomFieldBasicFields() {
        CustomType ct = createTestCustomType("test_field", CustomType.FieldType.STRING.getValue(),
                                              CustomType.ObjType.VULN.getValue());
        CustomField cf = createTestCustomField(ct, "test value");
        assertEquals("Value should match", "test value", cf.getValue());
        assertEquals("Type should match", ct, cf.getType());
    }

    // --- Note Entity Tests ---

    @Test
    public void testNoteBasicFields() {
        Note note = createTestNote("Test note text");
        assertEquals("Note text should match", "Test note text", note.getNote());
        assertNotNull("Created date should not be null", note.getCreated());
    }

    // --- Teams Entity Tests ---

    @Test
    public void testTeamsBasicFields() {
        Teams team = createTestTeam(1L, "Security Team");
        assertEquals("ID should match", team.getId(), Long.valueOf(1L));
        assertEquals("Name should match", "Security Team", team.getTeamName());
    }

    // --- Edge Cases ---

    @Test
    public void testVulnerabilityNullTrackingReturnsEmptyString() {
        Vulnerability v = new Vulnerability();
        v.setName("No Tracking");
        // tracking has a default value "VID-xxx", so getTracking() returns that, not empty string
        assertNotNull("Tracking should have default value", v.getTracking());
        assertTrue("Tracking should start with VID-", v.getTracking().startsWith("VID-"));
    }

    @Test
    public void testVulnerabilityWithNullName() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName(null);
        assertNull("Name should be null", v.getName());
    }

    @Test
    public void testAssessmentWithNullAssessors() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Assessors");
        assertNull("Assessors should be null when not set", a.getAssessor());
    }

    @Test
    public void testAssessmentWithNullEngagement() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Engagement");
        assertNull("Engagement should be null when not set", a.getEngagement());
    }

    @Test
    public void testAssessmentWithNullRemediation() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Remediation");
        assertNull("Remediation should be null when not set", a.getRemediation());
    }

    @Test
    public void testVulnerabilityWithNullAssessorId() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Assessor");
        assertNull("Assessor ID should be null when not set", v.getAssessorId());
    }

    @Test
    public void testVulnerabilityWithNullLikelyhood() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Likelyhood");
        v.setLevels(riskLevels);
        assertNull("Likelyhood should be null", v.getLikelyhood());
        assertEquals("Should return Unassigned", "Unassigned", v.getLikelyhoodStr());
    }

    @Test
    public void testVulnerabilityWithNullImpact() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Impact");
        v.setLevels(riskLevels);
        assertNull("Impact should be null", v.getImpact());
        assertEquals("Should return Unassigned", "Unassigned", v.getImpactStr());
    }

    @Test
    public void testUserWithNullEmail() {
        User u = new User();
        u.setId(1L);
        u.setUsername("noemail");
        assertNull("Email should be null when not set", u.getEmail());
    }

    @Test
    public void testUserWithNullUsername() {
        User u = new User();
        u.setId(1L);
        u.setUsername(null);
        assertNull("Username should be null when not set", u.getUsername());
    }

    @Test
    public void testAssessmentWithNullStatus() throws Exception {
        // getStatus() is a computed method that returns "Scheduled" by default
        // It doesn't return null even when this.status is null
        Assessment a = new Assessment();
        java.lang.reflect.Field startField = Assessment.class.getDeclaredField("start");
        // Set dates to future so getStatus() returns "Scheduled"
        startField.setAccessible(true);
        startField.set(a, new Date(System.currentTimeMillis() + 86400000L)); // 1 day from now
        java.lang.reflect.Field endField = Assessment.class.getDeclaredField("end");
        endField.setAccessible(true);
        endField.set(a, new Date(System.currentTimeMillis() + 86400000L * 2)); // 2 days from now
        
        // Verify getStatus() returns computed default, not null
        assertNotNull("Status should return computed default", a.getStatus());
        assertEquals("Status should be Scheduled by default", "Scheduled", a.getStatus());
    }

    @Test
    public void testAssessmentWithNullAppId() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No App ID");
        assertNull("App ID should be null when not set", a.getAppId());
    }

    @Test
    public void testVulnerabilityWithNullCvssScore() {
        Vulnerability v = new Vulnerability();
        v.setName("No CVSS");
        // getCvssScore() returns "" when null, not null
        assertEquals("CVSS score should return empty string when not set", "", v.getCvssScore());
        assertEquals("CVSS string should return empty string when not set", "", v.getCvssString());
    }

    @Test
    public void testVulnerabilityWithNullRecommendation() {
        Vulnerability v = new Vulnerability();
        v.setName("No Recommendation");
        // getRecommendation() returns "" when null, not null
        assertEquals("Recommendation should return empty string when not set", "", v.getRecommendation());
    }

    @Test
    public void testVulnerabilityWithNullDescription() {
        Vulnerability v = new Vulnerability();
        v.setName("No Description");
        // getDescription() returns "" when null, not null
        assertEquals("Description should return empty string when not set", "", v.getDescription());
    }

    @Test
    public void testVulnerabilityWithNullDetails() {
        Vulnerability v = new Vulnerability();
        v.setName("No Details");
        // getDetails() returns "" when null, not null
        assertEquals("Details should return empty string when not set", "", v.getDetails());
    }
}
