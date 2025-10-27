package com.fuse.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.Permissions;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Test class for Assessment API endpoints
 * 
 * Note: Due to the static nature of HibHelper and Support classes,
 * these tests focus on testing the API response handling and data structures
 * rather than full integration testing.
 */
public class AssessmentAPITest {
    
    private assessments assessmentAPI;
    
    // Test data
    private User testUser;
    private Assessment testAssessment;
    private AssessmentType testAssessmentType;
    private String apiKey = "test-api-key";
    private Long assessmentId = 1L;
    
    @Before
    public void setUp() {
        // Initialize API
        assessmentAPI = new assessments();
        
        // Set up test data
        testUser = createTestUser("testuser", true, false, false);
        testAssessmentType = createAssessmentType("Web Application");
        testAssessment = createTestAssessment(testUser, testAssessmentType);
        testAssessment.setId(assessmentId);
    }
    
    /**
     * Test that we can create an API instance
     */
    @Test
    public void testApiInstantiation() {
        assertNotNull("API instance should not be null", assessmentAPI);
    }
    
    /**
     * Test decoding and sanitization logic
     */
    @Test
    public void testDecodeAndSanitize() throws Exception {
        // Test with reflection since the method is private
        java.lang.reflect.Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);
        
        // Test basic base64 decoding
        String encoded = "VGhpcyBpcyBhIHRlc3Q="; // "This is a test"
        String result = (String) method.invoke(assessmentAPI, encoded);
        assertNotNull("Decoded result should not be null", result);
        
        // Test with empty string - the method may return a newline or empty string
        result = (String) method.invoke(assessmentAPI, "");
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty or just whitespace", result.trim().isEmpty());
    }
    
    /**
     * Test custom field handling logic
     */
    @Test
    public void testCustomFieldDataStructures() {
        // Test CustomType creation and properties
        CustomType ct = new CustomType();
        ct.setId(1L);
        ct.setKey("test_field");
        ct.setVariable("Test Field");
        ct.setType(CustomType.ObjType.ASMT.getValue());
        ct.setFieldType(CustomType.FieldType.STRING.getValue());
        
        assertEquals("Key should match", "test_field", ct.getKey());
        assertEquals("Variable should match", "Test Field", ct.getVariable());
        assertEquals("Type should be ASMT", CustomType.ObjType.ASMT.getValue(), ct.getType());
        assertEquals("Field type should be STRING", CustomType.FieldType.STRING.getValue(), ct.getFieldType());
        
        // Test CustomField creation
        CustomField cf = new CustomField();
        cf.setType(ct);
        cf.setValue("test value");
        
        assertEquals("Value should match", "test value", cf.getValue());
        assertNotNull("Type should not be null", cf.getType());
        assertEquals("Type key should match", "test_field", cf.getType().getKey());
    }
    
    /**
     * Test vulnerability data structures
     */
    @Test
    public void testVulnerabilityDataStructure() {
        Vulnerability vuln = createTestVulnerability(testAssessment);
        
        assertNotNull("Vulnerability should not be null", vuln);
        assertEquals("Name should match", "Test Vulnerability", vuln.getName());
        assertEquals("Assessment ID should match", testAssessment.getId(), Long.valueOf(vuln.getAssessmentId()));
        assertEquals("Overall score should match", Long.valueOf(7L), vuln.getOverall());
        assertEquals("Impact score should match", Long.valueOf(8L), vuln.getImpact());
        assertEquals("Likelihood score should match", Long.valueOf(6L), vuln.getLikelyhood());
    }
    
    /**
     * Test assessment data structures with custom fields
     */
    @Test 
    public void testAssessmentWithCustomFields() {
        // Add custom fields to assessment
        List<CustomField> customFields = new ArrayList<>();
        
        CustomType ct1 = new CustomType();
        ct1.setId(1L);
        ct1.setKey("impact_analysis");
        ct1.setVariable("Impact Analysis");
        ct1.setType(CustomType.ObjType.ASMT.getValue());
        ct1.setFieldType(CustomType.FieldType.STRING.getValue());
        
        CustomField cf1 = new CustomField();
        cf1.setType(ct1);
        cf1.setValue("High impact on business operations");
        customFields.add(cf1);
        
        testAssessment.setCustomFields(customFields);
        
        assertNotNull("Custom fields should not be null", testAssessment.getCustomFields());
        assertEquals("Should have one custom field", 1, testAssessment.getCustomFields().size());
        assertEquals("Custom field value should match", "High impact on business operations", 
                    testAssessment.getCustomFields().get(0).getValue());
    }
    
    /**
     * Test JSON serialization of custom fields
     */
    @Test
    public void testCustomFieldsJsonSerialization() throws Exception {
        Map<String, String> customFields = new HashMap<>();
        customFields.put("impact_analysis", "High impact");
        customFields.put("data_classification", "Confidential");
        
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(customFields);
        
        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain impact_analysis", json.contains("impact_analysis"));
        assertTrue("JSON should contain data_classification", json.contains("data_classification"));
        
        // Test deserialization
        Map<String, String> deserialized = mapper.readValue(json, 
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
        
        assertEquals("Should have 2 fields", 2, deserialized.size());
        assertEquals("Impact analysis should match", "High impact", deserialized.get("impact_analysis"));
        assertEquals("Data classification should match", "Confidential", deserialized.get("data_classification"));
    }
    
    /**
     * Test default vulnerability template structure
     */
    @Test
    public void testDefaultVulnerabilityTemplate() {
        DefaultVulnerability template = createVulnerabilityTemplate();
        
        assertNotNull("Template should not be null", template);
        assertEquals("Name should match", "SQL Injection", template.getName());
        assertEquals("Overall score should match", 4, template.getOverall());
        assertEquals("Impact score should match", 4, template.getImpact());
        assertEquals("Likelihood score should match", 4, template.getLikelyhood());
        assertEquals("CVSS 3.1 score should match", "8.6", template.getCvss31Score());
        assertNotNull("Category should not be null", template.getCategory());
    }
    
    /**
     * Helper methods to create test data
     */
    private User createTestUser(String username, boolean isAssessor, boolean isManager, boolean isEngagement) {
        User user = new User();
        // Generate unique ID based on username hash to avoid conflicts
        user.setId((long) username.hashCode());
        user.setUsername(username);
        
        Permissions perms = new Permissions();
        perms.setAssessor(isAssessor);
        perms.setManager(isManager);
        perms.setEngagement(isEngagement);
        user.setPermissions(perms);
        
        return user;
    }
    
    private AssessmentType createAssessmentType(String name) {
        AssessmentType type = new AssessmentType();
        type.setId(1L);
        type.setType(name);
        return type;
    }
    
    private Assessment createTestAssessment(User assessor, AssessmentType type) {
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setName("Test Assessment");
        assessment.setAppId("APP-123");
        assessment.setType(type);
        
        List<User> assessors = new ArrayList<>();
        assessors.add(assessor);
        assessment.setAssessor(assessors);
        
        assessment.setEngagement(assessor);
        assessment.setRemediation(assessor);
        assessment.setStart(new Date());
        assessment.setEnd(new Date());
        assessment.setVulns(new ArrayList<>());
        assessment.setCustomFields(new ArrayList<>());
        assessment.setDistributionList("test@example.com");
        
        Campaign camp = new Campaign();
        camp.setId(1L);
        camp.setName("Test Campaign");
        assessment.setCampaign(camp);
        
        return assessment;
    }
    
    private Vulnerability createTestVulnerability(Assessment assessment) {
        Vulnerability vuln = new Vulnerability();
        vuln.setId(1L);
        vuln.setName("Test Vulnerability");
        vuln.setDescription("Test vulnerability description");
        vuln.setRecommendation("Test recommendation");
        vuln.setAssessmentId(assessment.getId());
        vuln.setOverall(7L);
        vuln.setImpact(8L);
        vuln.setLikelyhood(6L);
        vuln.setCustomFields(new ArrayList<>());
        
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Web Application");
        vuln.setCategory(cat);
        
        return vuln;
    }
    
    private DefaultVulnerability createVulnerabilityTemplate() {
        DefaultVulnerability template = new DefaultVulnerability();
        template.setId(1L);
        template.setName("SQL Injection");
        template.setDescription("SQL injection vulnerability allows attackers to interfere with database queries");
        template.setRecommendation("Use parameterized queries and input validation");
        // Note: DefaultVulnerability might have different internal defaults
        // Using 4 as the default value based on the API code
        template.setOverall(4);
        template.setImpact(4);
        template.setLikelyhood(4);
        template.setCvss31Score("8.6");
        template.setCvss31String("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:N/A:N");
        
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");
        template.setCategory(cat);
        
        return template;
    }
    
    /**
     * Test access levels - allAssessments permission (AccessLevel = 0)
     */
    @Test
    public void testAllAssessmentsPermission() {
        // Create users with different permission levels
        User managerWithAllAccess = createTestUser("manager_all", false, true, false);
        managerWithAllAccess.getPermissions().setAccessLevel(Permissions.AccessLevelAllData); // 0
        
        User assessor1 = createTestUser("assessor1", true, false, false);
        User assessor2 = createTestUser("assessor2", true, false, false);
        User teamMember = createTestUser("team_member", true, false, false);
        
        // Create assessments owned by different users
        Assessment assessment1 = createTestAssessment(assessor1, testAssessmentType);
        assessment1.setId(100L);
        assessment1.setName("Assessor1's Assessment");
        
        Assessment assessment2 = createTestAssessment(assessor2, testAssessmentType);
        assessment2.setId(101L);
        assessment2.setName("Assessor2's Assessment");
        
        // Create team assessment
        List<User> teamAssessors = new ArrayList<>();
        teamAssessors.add(teamMember);
        teamAssessors.add(assessor1);
        Assessment teamAssessment = createTestAssessment(teamMember, testAssessmentType);
        teamAssessment.setAssessor(teamAssessors);
        teamAssessment.setId(102L);
        teamAssessment.setName("Team Assessment");
        
        // Test that manager with allAssessments can access all assessments
        assertNotNull("Manager with all access should exist", managerWithAllAccess);
        assertEquals("Should have AccessLevelAllData", Integer.valueOf(0),
                    managerWithAllAccess.getPermissions().getAccessLevel());
        assertTrue("Should be a manager", managerWithAllAccess.getPermissions().isManager());
        
        // In a real test, the manager would be able to see all three assessments
        // Here we verify the permission structure is correct
        List<Assessment> allAssessments = Arrays.asList(assessment1, assessment2, teamAssessment);
        assertEquals("Should have 3 assessments total", 3, allAssessments.size());
    }
    
    /**
     * Test access levels - teamAssessments permission (AccessLevel = 1)
     */
    @Test
    public void testTeamAssessmentsPermission() {
        // Create user with team-only access
        User teamUser = createTestUser("team_user", true, false, false);
        teamUser.getPermissions().setAccessLevel(Permissions.AccessLevelTeamOnly); // 1
        
        User otherUser = createTestUser("other_user", true, false, false);
        
        // Create assessment where teamUser is part of the team
        List<User> team = new ArrayList<>();
        team.add(teamUser);
        team.add(otherUser);
        Assessment teamAssessment = createTestAssessment(teamUser, testAssessmentType);
        teamAssessment.setAssessor(team);
        teamAssessment.setId(200L);
        teamAssessment.setName("Team Assessment");
        
        // Create assessment where teamUser is NOT part of the team
        Assessment otherAssessment = createTestAssessment(otherUser, testAssessmentType);
        otherAssessment.setId(201L);
        otherAssessment.setName("Other User's Assessment");
        
        // Test permission structure
        assertNotNull("Team user should exist", teamUser);
        assertEquals("Should have AccessLevelTeamOnly", Integer.valueOf(1),
                    teamUser.getPermissions().getAccessLevel());
        
        // Verify team membership
        boolean isInTeam = false;
        for (User assessor : teamAssessment.getAssessor()) {
            if (assessor.getId() == teamUser.getId()) {
                isInTeam = true;
                break;
            }
        }
        assertTrue("Team user should be in team assessment", isInTeam);
        
        // otherAssessment only has otherUser as assessor, teamUser is not in it
        assertEquals("Other assessment should only have one assessor", 1, otherAssessment.getAssessor().size());
        assertEquals("Other assessment assessor should be otherUser", otherUser.getId(),
                    otherAssessment.getAssessor().get(0).getId());
    }
    
    /**
     * Test access levels - ownedAssessments permission (AccessLevel = 2)
     */
    @Test
    public void testOwnedAssessmentsPermission() {
        // Create user with owned-only access
        User ownedOnlyUser = createTestUser("owned_only_user", true, false, false);
        ownedOnlyUser.getPermissions().setAccessLevel(Permissions.AccessLevelUserOnly); // 2
        
        User otherUser = createTestUser("other_assessor", true, false, false);
        
        // Create assessment owned by ownedOnlyUser
        Assessment ownedAssessment = createTestAssessment(ownedOnlyUser, testAssessmentType);
        ownedAssessment.setId(300L);
        ownedAssessment.setName("My Own Assessment");
        
        // Create team assessment with otherUser as primary, ownedOnlyUser as secondary
        List<User> team = new ArrayList<>();
        team.add(otherUser); // Primary assessor
        team.add(ownedOnlyUser); // Secondary assessor
        Assessment teamAssessment = createTestAssessment(otherUser, testAssessmentType);
        teamAssessment.setAssessor(team);
        teamAssessment.setId(301L);
        teamAssessment.setName("Team Assessment");
        
        // Create assessment NOT owned by ownedOnlyUser
        Assessment otherAssessment = createTestAssessment(otherUser, testAssessmentType);
        otherAssessment.setId(302L);
        otherAssessment.setName("Other Assessment");
        
        // Test permission structure
        assertNotNull("Owned-only user should exist", ownedOnlyUser);
        assertEquals("Should have AccessLevelUserOnly", Integer.valueOf(2),
                    ownedOnlyUser.getPermissions().getAccessLevel());
        
        // Verify ownership - user should only see assessments where they are the primary assessor
        boolean isOwnerOfOwned = false;
        for (User assessor : ownedAssessment.getAssessor()) {
            if (assessor.getId() == ownedOnlyUser.getId()) {
                isOwnerOfOwned = true;
                break;
            }
        }
        assertTrue("Should be owner of owned assessment", isOwnerOfOwned);
        
        // Even though user is in team, with AccessLevelUserOnly they shouldn't see it
        // unless they are the primary owner
        boolean isInTeam = false;
        for (User assessor : teamAssessment.getAssessor()) {
            if (assessor.getId() == ownedOnlyUser.getId()) {
                isInTeam = true;
                break;
            }
        }
        assertTrue("Should be in team assessment", isInTeam);
        
        // But the primary assessor is otherUser, not ownedOnlyUser
        assertEquals("Primary assessor should be otherUser",
                    otherUser.getId(), teamAssessment.getAssessor().get(0).getId());
        assertNotEquals("Should not be primary assessor of team assessment",
                       ownedOnlyUser.getId(), teamAssessment.getAssessor().get(0).getId());
    }
    
    /**
     * Test different role-based permissions
     */
    @Test
    public void testRoleBasedPermissions() {
        // Test Assessor permissions
        User assessor = createTestUser("assessor", true, false, false);
        assertTrue("Should be assessor", assessor.getPermissions().isAssessor());
        assertFalse("Should not be manager", assessor.getPermissions().isManager());
        assertFalse("Should not be engagement", assessor.getPermissions().isEngagement());
        
        // Test Manager permissions
        User manager = createTestUser("manager", false, true, false);
        assertFalse("Should not be assessor", manager.getPermissions().isAssessor());
        assertTrue("Should be manager", manager.getPermissions().isManager());
        assertFalse("Should not be engagement", manager.getPermissions().isEngagement());
        
        // Test Engagement permissions
        User engagement = createTestUser("engagement", false, false, true);
        assertFalse("Should not be assessor", engagement.getPermissions().isAssessor());
        assertFalse("Should not be manager", engagement.getPermissions().isManager());
        assertTrue("Should be engagement", engagement.getPermissions().isEngagement());
        
        // Test combined permissions
        User multiRole = createTestUser("multirole", true, true, true);
        assertTrue("Should be assessor", multiRole.getPermissions().isAssessor());
        assertTrue("Should be manager", multiRole.getPermissions().isManager());
        assertTrue("Should be engagement", multiRole.getPermissions().isEngagement());
        
        // Test remediation role
        User remediation = createTestUser("remediation", false, false, false);
        remediation.getPermissions().setRemediation(true);
        assertTrue("Should be remediation", remediation.getPermissions().isRemediation());
        
        // Test executive role
        User executive = createTestUser("executive", false, false, false);
        executive.getPermissions().setExecutive(true);
        assertTrue("Should be executive", executive.getPermissions().isExecutive());
    }
    
    /**
     * Test engagement and remediation user access to assessments
     */
    @Test
    public void testEngagementAndRemediationAccess() {
        User assessorUser = createTestUser("assessor", true, false, false);
        User engagementUser = createTestUser("engagement", false, false, true);
        User remediationUser = createTestUser("remediation", false, false, false);
        remediationUser.getPermissions().setRemediation(true);
        
        // Create assessment with specific engagement and remediation contacts
        Assessment assessment = createTestAssessment(assessorUser, testAssessmentType);
        assessment.setEngagement(engagementUser);
        assessment.setRemediation(remediationUser);
        assessment.setId(400L);
        
        // Test that engagement user is properly set
        assertNotNull("Engagement user should be set", assessment.getEngagement());
        assertEquals("Engagement user ID should match", engagementUser.getId(),
                    assessment.getEngagement().getId());
        assertTrue("Engagement user should have engagement permission",
                  assessment.getEngagement().getPermissions().isEngagement());
        
        // Test that remediation user is properly set
        assertNotNull("Remediation user should be set", assessment.getRemediation());
        assertEquals("Remediation user ID should match", remediationUser.getId(),
                    assessment.getRemediation().getId());
        assertTrue("Remediation user should have remediation permission",
                  assessment.getRemediation().getPermissions().isRemediation());
        
        // Test that assessor is properly set
        assertFalse("Assessor list should not be empty", assessment.getAssessor().isEmpty());
        assertEquals("Should have one assessor", 1, assessment.getAssessor().size());
        assertEquals("Assessor ID should match", assessorUser.getId(),
                    assessment.getAssessor().get(0).getId());
    }
}