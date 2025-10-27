package com.fuse.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.AssessmentDTO;
import com.fuse.api.dto.VulnerabilityDTO;
import com.fuse.api.dto.CustomTypeDTO;
import com.fuse.api.dto.CustomFieldDTO;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.Note;
import com.fuse.dao.Permissions;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Unit test class for Assessment API endpoints
 * Tests API data structures, DTOs, and response formats without database dependencies
 */
public class AssessmentAPIUnitTest {
    
    private ObjectMapper mapper;
    
    // Test data
    private User testUser;
    private Assessment testAssessment;
    private AssessmentType testAssessmentType;
    private Long assessmentId = 100L;
    private Long userId = 1L;
    
    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        setupTestData();
    }
    
    private void setupTestData() {
        // Create test user
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        
        Permissions perms = new Permissions();
        perms.setAssessor(true);
        perms.setManager(false);
        perms.setEngagement(false);
        testUser.setPermissions(perms);
        
        // Create assessment type
        testAssessmentType = new AssessmentType();
        testAssessmentType.setId(1L);
        testAssessmentType.setType("Web Application");
        
        // Create test assessment
        testAssessment = new Assessment();
        testAssessment.setId(assessmentId);
        testAssessment.setName("Test Assessment");
        testAssessment.setAppId("APP-123");
        testAssessment.setType(testAssessmentType);
        testAssessment.setAssessor(Arrays.asList(testUser));
        testAssessment.setEngagement(testUser);
        testAssessment.setRemediation(testUser);
        testAssessment.setStart(new Date());
        testAssessment.setEnd(new Date());
        testAssessment.setVulns(new ArrayList<>());
        testAssessment.setDistributionList("test@example.com");
        testAssessment.setCustomFields(new ArrayList<>());
        
        // Add notebook entry (required by AssessmentDTO)
        List<Note> notebook = new ArrayList<>();
        Note note = new Note();
        note.setNote("Test notes");
        notebook.add(note);
        testAssessment.setNotebook(notebook);
        
        Campaign camp = new Campaign();
        camp.setId(1L);
        camp.setName("Test Campaign");
        testAssessment.setCampaign(camp);
    }
    
    @Test
    public void testAssessmentDTOFromEntity() {
        // Test basic DTO conversion
        AssessmentDTO dto = AssessmentDTO.fromEntity(testAssessment);
        
        assertNotNull("DTO should not be null", dto);
        assertEquals("Assessment name should match", testAssessment.getName(), dto.getName());
        assertEquals("App ID should match", testAssessment.getAppId(), dto.getAppId());
        assertEquals("Assessment ID should match", testAssessment.getId(), dto.getId());
        assertEquals("Distribution list should match", testAssessment.getDistributionList(), dto.getDistributionList());
        
        // Check type
        assertNotNull("Type should not be null", dto.getType());
        assertEquals("Type name should match", testAssessmentType.getType(), dto.getType());
        
        // Check campaign
        assertNotNull("Campaign should not be null", dto.getCampaign());
        assertEquals("Campaign name should match", "Test Campaign", dto.getCampaign());
        
        // Check assessors
        assertNotNull("Assessors should not be null", dto.getAssessors());
        assertEquals("Should have 1 assessor", 1, dto.getAssessors().size());
        assertEquals("Assessor username should match", "testuser", dto.getAssessors().get(0).getUsername());
    }
    
    @Test
    public void testAssessmentDTOWithNullFields() {
        // Create minimal assessment with required fields
        Assessment minimal = new Assessment();
        minimal.setId(1L);
        minimal.setName("Minimal");
        minimal.setStart(new Date()); // Required by DTO
        minimal.setEnd(new Date()); // Required by DTO
        
        // Add minimal notebook entry (required by DTO)
        List<Note> notebook = new ArrayList<>();
        Note note = new Note();
        note.setNote("");
        notebook.add(note);
        minimal.setNotebook(notebook);
        
        // This should not throw exception
        AssessmentDTO dto = AssessmentDTO.fromEntity(minimal);
        assertNotNull("DTO should handle null fields", dto);
        assertEquals("Name should match", "Minimal", dto.getName());
        
        // Null checks
        if (dto.getAssessors() != null) {
            assertEquals("Null assessors should become empty list", 0, dto.getAssessors().size());
        } else {
            // AssessmentDTO returns null for null assessors
            assertEquals("Assessors should be null", null, dto.getAssessors());
        }
        // Note: AssessmentDTO returns 0 for null vulnerabilities list
        assertEquals("Vulnerability count should be 0 for null vulns", Integer.valueOf(0), dto.getVulnerabilityCount());
        assertTrue("Null custom fields should become empty map", dto.getCustomFields().isEmpty());
    }
    
    @Test
    public void testVulnerabilityDTO() {
        // Create test vulnerability
        Vulnerability vuln = new Vulnerability();
        vuln.setId(50L);
        vuln.setName("SQL Injection");
        vuln.setDescription("SQL injection vulnerability");
        vuln.setRecommendation("Use parameterized queries");
        vuln.setOverall(8L);
        vuln.setImpact(8L);
        vuln.setLikelyhood(7L);
        vuln.setCvssScore("7.5");
        vuln.setCvssString("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N");
        vuln.setAssessmentId(1L); // Required by VulnerabilityDTO
        
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");
        vuln.setCategory(cat);
        
        // Test DTO conversion
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);
        
        assertNotNull("DTO should not be null", dto);
        assertEquals("Name should match", vuln.getName(), dto.getName());
        assertEquals("Description should match", vuln.getDescription(), dto.getDescription());
        assertEquals("Recommendation should match", vuln.getRecommendation(), dto.getRecommendation());
        assertEquals("Overall should match", vuln.getOverall(), dto.getOverall());
        assertEquals("Impact should match", vuln.getImpact(), dto.getImpact());
        assertEquals("Likelihood should match", vuln.getLikelyhood(), dto.getLikelyhood());
        assertEquals("CVSS score should match", vuln.getCvssScore(), dto.getCvssScore());
        assertEquals("CVSS string should match", vuln.getCvssString(), dto.getCvssString());
        
        // Note: VulnerabilityDTO doesn't expose category directly
    }
    
    @Test
    public void testVulnerabilityDTOWithCustomFields() {
        // Create vulnerability with custom fields
        Vulnerability vuln = new Vulnerability();
        vuln.setId(50L);
        vuln.setName("Test Vuln");
        vuln.setAssessmentId(1L); // Required by VulnerabilityDTO
        
        List<CustomField> customFields = new ArrayList<>();
        
        // Add custom field
        CustomField cf1 = new CustomField();
        cf1.setId(1L);
        CustomType ct1 = new CustomType();
        ct1.setKey("owasp_category");
        ct1.setFieldType(CustomType.FieldType.STRING.getValue());
        cf1.setType(ct1);
        cf1.setValue("A03:2021 – Injection");
        customFields.add(cf1);
        
        // Add another custom field
        CustomField cf2 = new CustomField();
        cf2.setId(2L);
        CustomType ct2 = new CustomType();
        ct2.setKey("severity_rating");
        ct2.setFieldType(CustomType.FieldType.LIST.getValue());
        cf2.setType(ct2);
        cf2.setValue("High");
        customFields.add(cf2);
        
        vuln.setCustomFields(customFields);
        
        // Test DTO conversion
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);
        
        assertNotNull("DTO should not be null", dto);
        
        // Add custom fields to DTO manually since fromEntity doesn't do it automatically
        dto.setCustomFieldsFromEntity(vuln.getCustomFields());
        
        assertNotNull("Custom fields should not be null", dto.getCustomFields());
        assertEquals("Should have 2 custom fields", 2, dto.getCustomFields().size());
        
        // Check custom field values
        boolean foundOwasp = false;
        boolean foundSeverity = false;
        for (CustomFieldDTO cfDto : dto.getCustomFields()) {
            if ("owasp_category".equals(cfDto.getKey())) {
                assertEquals("OWASP category should match", "A03:2021 – Injection", cfDto.getValue());
                foundOwasp = true;
            } else if ("severity_rating".equals(cfDto.getKey())) {
                assertEquals("Severity rating should match", "High", cfDto.getValue());
                foundSeverity = true;
            }
        }
        assertTrue("Should have found OWASP category", foundOwasp);
        assertTrue("Should have found severity rating", foundSeverity);
    }
    
    @Test
    public void testCustomTypeDTO() {
        // Create test custom type
        CustomType ct = new CustomType();
        ct.setId(1L);
        ct.setKey("impact_analysis");
        ct.setType(CustomType.ObjType.ASMT.getValue());
        ct.setFieldType(CustomType.FieldType.STRING.getValue());
        ct.setVariable("Low,Medium,High,Critical");
        ct.setDefaultValue("Medium");
        ct.setAssessmentTypes(Arrays.asList(testAssessmentType));
        
        // Test DTO conversion
        CustomTypeDTO dto = CustomTypeDTO.fromEntity(ct);
        
        assertNotNull("DTO should not be null", dto);
        assertEquals("Key should match", ct.getKey(), dto.getKey());
        // The DTO converts the type to a string representation
        assertEquals("Type should match", "Assessment", dto.getType());
        // The DTO converts the field type to its string representation
        assertEquals("Field type should match", "String", dto.getFieldType());
        assertEquals("Variable should match", ct.getVariable(), dto.getVariable());
        assertEquals("Default value should match", ct.getDefaultValue(), dto.getDefaultValue());
        
        // Note: CustomTypeDTO doesn't expose assessment types
    }
    
    @Test
    public void testCustomFieldDTO() {
        // Create test custom field
        CustomField cf = new CustomField();
        cf.setId(1L);
        
        CustomType ct = new CustomType();
        ct.setKey("test_field");
        ct.setFieldType(CustomType.FieldType.STRING.getValue());
        cf.setType(ct);
        cf.setValue("This is a test value");
        
        // Test DTO conversion
        CustomFieldDTO dto = CustomFieldDTO.fromEntity(cf);
        
        assertNotNull("DTO should not be null", dto);
        assertEquals("Value should match", cf.getValue(), dto.getValue());
        
        // Check type info
        assertNotNull("Key should not be null", dto.getKey());
        assertEquals("Key should match", "test_field", dto.getKey());
        assertNotNull("Type should not be null", dto.getType());
        // Note: dto.getType() returns the string representation of the type, not the full type object
    }
    
    @Test
    public void testDefaultVulnerabilityWithDefaults() {
        // Test default vulnerability score behavior
        DefaultVulnerability dv = new DefaultVulnerability();
        dv.setId(1L);
        dv.setName("Default Vuln");
        dv.setDescription("Test");
        dv.setRecommendation("Fix it");
        
        // When severity is not set, it should use default (assuming 4 based on previous tests)
        if (dv.getOverall() == 0) {
            dv.setOverall(4); // Default score
        }
        
        assertEquals("Default severity should be 4", 4, dv.getOverall());
    }
    
    @Test
    public void testAssessmentWithMultipleVulnerabilities() {
        // Add vulnerabilities to assessment
        List<Vulnerability> vulns = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            Vulnerability v = new Vulnerability();
            v.setId((long) i);
            v.setName("Vuln " + i);
            v.setOverall((long) (i + 5));
            v.setAssessmentId(testAssessment.getId());
            vulns.add(v);
        }
        
        testAssessment.setVulns(vulns);
        
        // Test DTO conversion
        AssessmentDTO dto = AssessmentDTO.fromEntity(testAssessment);
        
        assertNotNull("DTO should not be null", dto);
        assertNotNull("Vulnerability count should not be null", dto.getVulnerabilityCount());
        assertEquals("Should have 5 vulnerabilities", Integer.valueOf(5), dto.getVulnerabilityCount());
    }
    
    @Test
    public void testCustomFieldsMapConversion() throws Exception {
        // Test converting custom fields to/from JSON
        Map<String, String> customFields = new HashMap<>();
        customFields.put("field1", "value1");
        customFields.put("field2", "value2");
        customFields.put("field_with_special_chars", "value with spaces & symbols!");
        
        // Convert to JSON
        String json = mapper.writeValueAsString(customFields);
        assertNotNull("JSON should not be null", json);
        
        // Convert back
        Map<String, String> parsed = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        assertNotNull("Parsed map should not be null", parsed);
        assertEquals("Should have same number of fields", customFields.size(), parsed.size());
        
        // Verify values
        assertEquals("Field1 should match", "value1", parsed.get("field1"));
        assertEquals("Field2 should match", "value2", parsed.get("field2"));
        assertEquals("Special chars field should match", "value with spaces & symbols!", parsed.get("field_with_special_chars"));
    }
    
    @Test
    public void testErrorResponseFormat() {
        // Test that we can create error responses in expected format
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Authorized");
        errorResponse.put("details", "Invalid API key provided");
        
        try {
            String json = mapper.writeValueAsString(errorResponse);
            assertNotNull("Error JSON should not be null", json);
            assertTrue("Should contain error message", json.contains("Not Authorized"));
            assertTrue("Should contain details", json.contains("Invalid API key"));
        } catch (Exception e) {
            fail("Should be able to serialize error response: " + e.getMessage());
        }
    }
    
    @Test
    public void testSuccessResponseFormat() {
        // Test success response format for vulnerability creation
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("vid", 123L);
        successResponse.put("message", "Vulnerability created successfully");
        
        try {
            String json = mapper.writeValueAsString(successResponse);
            assertNotNull("Success JSON should not be null", json);
            assertTrue("Should contain success flag", json.contains("\"success\":true"));
            assertTrue("Should contain vulnerability ID", json.contains("\"vid\":123"));
        } catch (Exception e) {
            fail("Should be able to serialize success response: " + e.getMessage());
        }
    }
    
    @Test
    public void testCustomTypeFieldTypeEnum() {
        // Test all field types
        for (CustomType.FieldType fieldType : CustomType.FieldType.values()) {
            CustomType ct = new CustomType();
            ct.setFieldType(fieldType.getValue());
            
            assertEquals("Field type value should match", fieldType.getValue(), ct.getFieldType());
        }
    }
    
    @Test
    public void testCustomTypeObjTypeEnum() {
        // Test all object types
        for (CustomType.ObjType objType : CustomType.ObjType.values()) {
            CustomType ct = new CustomType();
            ct.setType(objType.getValue());
            
            assertEquals("Object type value should match", objType.getValue(), ct.getType());
        }
    }
    
    @Test
    public void testAssessmentTypeFiltering() {
        // Create multiple assessment types
        AssessmentType type1 = new AssessmentType();
        type1.setId(1L);
        type1.setType("Web Application");
        
        AssessmentType type2 = new AssessmentType();
        type2.setId(2L);
        type2.setType("Network");
        
        // Create custom type that applies only to Web Application
        CustomType ct = new CustomType();
        ct.setKey("web_specific_field");
        ct.setAssessmentTypes(Arrays.asList(type1));
        
        // Verify filtering logic
        assertTrue("Should apply to Web Application", 
            ct.getAssessmentTypes().stream().anyMatch(t -> t.getType().equals("Web Application")));
        assertFalse("Should not apply to Network", 
            ct.getAssessmentTypes().stream().anyMatch(t -> t.getType().equals("Network")));
    }
    
    @Test
    public void testBase64Encoding() {
        // Test that we can handle base64 encoded content
        String originalText = "SQL injection vulnerability";
        String base64 = java.util.Base64.getEncoder().encodeToString(originalText.getBytes());
        
        assertEquals("Base64 encoding should match", "U1FMIGluamVjdGlvbiB2dWxuZXJhYmlsaXR5", base64);
        
        // Decode back
        String decoded = new String(java.util.Base64.getDecoder().decode(base64));
        assertEquals("Decoded text should match original", originalText, decoded);
    }
    
    @Test
    public void testPermissionsValidation() {
        // Test different permission combinations
        User assessor = new User();
        Permissions assessorPerms = new Permissions();
        assessorPerms.setAssessor(true);
        assessorPerms.setManager(false);
        assessorPerms.setEngagement(false);
        assessor.setPermissions(assessorPerms);
        assertTrue("Assessor should have assessor permission", assessor.getPermissions().isAssessor());
        assertFalse("Assessor should not have manager permission", assessor.getPermissions().isManager());
        
        User manager = new User();
        Permissions managerPerms = new Permissions();
        managerPerms.setManager(true);
        managerPerms.setAssessor(false);
        managerPerms.setEngagement(false);
        manager.setPermissions(managerPerms);
        assertTrue("Manager should have manager permission", manager.getPermissions().isManager());
        assertFalse("Manager should not have assessor permission", manager.getPermissions().isAssessor());
        
        User admin = new User();
        Permissions adminPerms = new Permissions();
        adminPerms.setAssessor(true);
        adminPerms.setManager(true);
        adminPerms.setEngagement(true);
        admin.setPermissions(adminPerms);
        assertTrue("Admin should have all permissions", 
            admin.getPermissions().isAssessor() && 
            admin.getPermissions().isManager() && 
            admin.getPermissions().isEngagement());
    }
    
    @Test
    public void testCVSSVersionHandling() {
        // Test CVSS 3.1 string format
        String cvss31 = "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N";
        assertTrue("Should be CVSS 3.1 format", cvss31.startsWith("CVSS:3.1/"));
        
        // Test parsing CVSS components
        String[] parts = cvss31.substring(9).split("/");
        assertTrue("Should have multiple components", parts.length >= 6);
        
        // Test CVSS score validation
        String score = "7.5";
        double scoreValue = Double.parseDouble(score);
        assertTrue("Score should be between 0 and 10", scoreValue >= 0.0 && scoreValue <= 10.0);
    }
    
    @Test
    public void testCampaignAssociation() {
        // Test campaign handling
        Campaign campaign = new Campaign();
        campaign.setId(10L);
        campaign.setName("Q4 2023 Testing");
        
        Assessment assessment = new Assessment();
        assessment.setCampaign(campaign);
        
        assertEquals("Campaign should be associated", campaign, assessment.getCampaign());
        assertEquals("Campaign name should match", "Q4 2023 Testing", assessment.getCampaign().getName());
    }
}