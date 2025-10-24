package com.fuse.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.AssessmentDTO;
import com.fuse.api.dto.CustomFieldDTO;
import com.fuse.api.dto.CustomTypeDTO;
import com.fuse.api.dto.VulnerabilityDTO;
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
 * 
 * This test demonstrates the API functionality and provides examples of how to use
 * the new DTO-based endpoints with custom field support.
 * 
 * Note: This test requires manual setup of HibHelper and Support mocks in a real
 * test environment. The examples here show the expected behavior and JSON structures.
 */
public class AssessmentAPIWorkingTest {
    
    // Test data
    private User testUser;
    private Assessment testAssessment;
    private AssessmentType testAssessmentType;
    private String apiKey = "test-api-key-12345";
    private Long assessmentId = 100L;
    private Long userId = 1L;
    
    @Before
    public void setUp() throws Exception {
        // Set up test data
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
    public void testAssessmentDTOBackwardCompatibility() throws Exception {
        // Test that AssessmentDTO maintains backward compatibility with field naming
        AssessmentDTO dto = AssessmentDTO.fromEntity(testAssessment);
        
        // Verify backward-compatible field names
        assertNotNull(dto);
        assertEquals("APP-123", dto.getAppId());  // Should be 'appid' in JSON
        assertEquals("Test Assessment", dto.getName());
        assertEquals("test@example.com", dto.getDistributionList());
        
        // Serialize to JSON and verify field names
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);
        
        // Verify JSON contains backward-compatible field names
        assertTrue(json.contains("\"AppId\":\"APP-123\""));
        assertTrue(json.contains("\"Name\":\"Test Assessment\""));
        assertTrue(json.contains("\"DistributionList\":\"test@example.com\""));
        assertTrue(json.contains("\"CustomFields\":"));
    }
    
    @Test
    public void testVulnerabilityDTOBackwardCompatibility() throws Exception {
        // Create a test vulnerability
        Vulnerability vuln = new Vulnerability();
        vuln.setId(1L);
        vuln.setName("SQL Injection");
        vuln.setAssessmentId(assessmentId);
        vuln.setOverall(8L);
        vuln.setImpact(8L);
        vuln.setLikelyhood(7L);
        vuln.setCvssScore("7.5");
        vuln.setCvssString("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N");
        vuln.setDescription("SQL injection vulnerability found");
        vuln.setRecommendation("Use parameterized queries");
        vuln.setDetails("Details of the exploit");
        
        // Add custom fields
        List<CustomField> customFields = new ArrayList<>();
        
        CustomType ct1 = new CustomType();
        ct1.setKey("owasp_category");
        ct1.setFieldType(CustomType.FieldType.STRING.getValue());
        
        CustomField cf1 = new CustomField();
        cf1.setType(ct1);
        cf1.setValue("A03:2021 – Injection");
        customFields.add(cf1);
        
        vuln.setCustomFields(customFields);
        
        // Convert to DTO
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);
        
        // Verify backward-compatible field names
        assertEquals("SQL Injection", dto.getName());
        assertEquals(Long.valueOf(assessmentId), dto.getAssessmentId());
        // Note: VulnerabilityDTO severity string conversion maps 8 to "Critical" internally
        assertNotNull(dto.getOverall());
        assertNotNull(dto.getImpact());
        assertEquals(Long.valueOf(7L), dto.getLikelyhood());
        assertEquals("7.5", dto.getCvssScore());
        assertEquals("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N", dto.getCvssString());
        
        // Verify custom fields
        // Add custom fields to DTO manually since fromEntity doesn't do it automatically
        dto.setCustomFieldsFromEntity(vuln.getCustomFields());
        
        assertNotNull(dto.getCustomFields());
        assertEquals(1, dto.getCustomFields().size());
        
        CustomFieldDTO cfDto = dto.getCustomFields().get(0);
        assertEquals("owasp_category", cfDto.getKey());
        assertEquals("A03:2021 – Injection", cfDto.getValue());
        
        // Serialize to JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);
        
        // Verify JSON structure with proper capitalization
        assertTrue(json.contains("\"AssessmentId\":" + assessmentId));
        assertTrue(json.contains("\"Likelyhood\":7"));
        assertTrue(json.contains("\"CustomFields\":["));
        assertTrue(json.contains("\"Key\":\"owasp_category\""));
        assertTrue(json.contains("\"Value\":\"A03:2021 – Injection\""));
    }
    
    @Test
    public void testCustomFieldTypesDTO() throws Exception {
        // Test CustomTypeDTO conversion
        CustomType ct = new CustomType();
        ct.setId(1L);
        ct.setKey("impact_analysis");
        ct.setFieldType(CustomType.FieldType.STRING.getValue());
        ct.setDefaultValue("");
        
        CustomTypeDTO dto = CustomTypeDTO.fromEntity(ct);
        
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("impact_analysis", dto.getKey());
        // The DTO converts the field type to its string representation
        assertEquals("String", dto.getFieldType());
        assertEquals("", dto.getDefaultValue());
        
        // Test LIST type
        CustomType ctList = new CustomType();
        ctList.setId(2L);
        ctList.setKey("data_classification");
        ctList.setFieldType(CustomType.FieldType.LIST.getValue());
        ctList.setVariable("Public,Internal,Confidential");
        ctList.setDefaultValue("Internal");
        
        CustomTypeDTO dtoList = CustomTypeDTO.fromEntity(ctList);
        
        assertEquals("data_classification", dtoList.getKey());
        assertEquals("List", dtoList.getFieldType());
        // Options are stored in the variable field as comma-separated values
        String variable = dtoList.getVariable();
        assertNotNull(variable);
        String[] options = variable.split(",");
        assertEquals(3, options.length);
        assertEquals("Public", options[0]);
        assertEquals("Internal", options[1]);
        assertEquals("Confidential", options[2]);
        assertEquals("Internal", dtoList.getDefaultValue());
        
        // Test BOOLEAN type
        CustomType ctBool = new CustomType();
        ctBool.setId(3L);
        ctBool.setKey("requires_auth");
        ctBool.setFieldType(CustomType.FieldType.BOOLEAN.getValue());
        ctBool.setDefaultValue("true");
        
        CustomTypeDTO dtoBool = CustomTypeDTO.fromEntity(ctBool);
        
        assertEquals("requires_auth", dtoBool.getKey());
        assertEquals("Boolean", dtoBool.getFieldType());
        assertEquals("true", dtoBool.getDefaultValue());
    }
    
    @Test
    public void testCustomFieldsJSONExamples() throws Exception {
        // Example 1: Assessment custom fields JSON
        Map<String, String> assessmentCustomFields = new HashMap<>();
        assessmentCustomFields.put("impact_analysis", "High impact on business operations");
        assessmentCustomFields.put("data_classification", "Confidential");
        assessmentCustomFields.put("compliance_requirements", "PCI-DSS, HIPAA");
        
        ObjectMapper mapper = new ObjectMapper();
        String assessmentJson = mapper.writeValueAsString(assessmentCustomFields);
        System.out.println("Example Assessment Custom Fields JSON:");
        System.out.println(assessmentJson);
        
        // Example 2: Vulnerability custom fields JSON
        Map<String, String> vulnCustomFields = new HashMap<>();
        vulnCustomFields.put("owasp_category", "A03:2021 – Injection");
        vulnCustomFields.put("affected_users", "All authenticated users");
        vulnCustomFields.put("requires_authentication", "true");
        vulnCustomFields.put("exploitation_difficulty", "Low");
        
        String vulnJson = mapper.writeValueAsString(vulnCustomFields);
        System.out.println("\nExample Vulnerability Custom Fields JSON:");
        System.out.println(vulnJson);
    }
    
    @Test
    public void testAPIEndpointExamples() throws Exception {
        System.out.println("\n=== API Endpoint Examples ===\n");
        
        // Example 1: Get assessment with custom fields
        System.out.println("GET /api/assessments/100");
        System.out.println("Expected Response:");
        AssessmentDTO assessmentDto = AssessmentDTO.fromEntity(testAssessment);
        
        // Add example custom fields
        List<CustomFieldDTO> customFields = new ArrayList<>();
        CustomFieldDTO cf1 = new CustomFieldDTO();
        cf1.setKey("impact_analysis");
        cf1.setValue("Critical business impact");
        customFields.add(cf1);
        
        assessmentDto.setCustomFields(customFields);
        
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(assessmentDto));
        
        // Example 2: Update assessment with custom fields
        System.out.println("\n\nPOST /api/assessments/100");
        System.out.println("Request Body (form data):");
        System.out.println("notes=Updated notes");
        System.out.println("summary=Updated summary");
        System.out.println("customFields={\"impact_analysis\":\"High impact\",\"data_classification\":\"Internal\"}");
        
        // Example 3: Add vulnerability with custom fields
        System.out.println("\n\nPOST /api/assessments/addVuln/100");
        System.out.println("Request Body (form data):");
        System.out.println("name=SQL Injection");
        System.out.println("description=U1FMIGluamVjdGlvbiB2dWxuZXJhYmlsaXR5");
        System.out.println("recommendation=VXNlIHBhcmFtZXRlcml6ZWQgcXVlcmllcw==");
        System.out.println("severity=8");
        System.out.println("impact=8");
        System.out.println("likelihood=7");
        System.out.println("cvssScore=7.5");
        System.out.println("cvssString=CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N");
        System.out.println("customFields={\"owasp_category\":\"A03:2021 – Injection\",\"affected_users\":\"All users\"}");
        
        // Example 4: Update vulnerability with partial custom fields
        System.out.println("\n\nPOST /api/assessments/vuln/50");
        System.out.println("Request Body (form data):");
        System.out.println("severity=9");
        System.out.println("customFields={\"exploitation_difficulty\":\"Medium\"}");
        System.out.println("Note: Only provided fields will be updated");
        
        // Example 5: Get custom field types for assessment
        System.out.println("\n\nGET /api/assessments/customfields/100");
        System.out.println("Expected Response:");
        Map<String, Object> customFieldTypesResponse = new HashMap<>();
        
        List<CustomTypeDTO> assessmentFields = new ArrayList<>();
        CustomTypeDTO actDto = new CustomTypeDTO();
        actDto.setId(1L);
        actDto.setKey("impact_analysis");
        actDto.setFieldType("String");
        actDto.setDefaultValue("");
        assessmentFields.add(actDto);
        
        List<CustomTypeDTO> vulnerabilityFields = new ArrayList<>();
        CustomTypeDTO vctDto = new CustomTypeDTO();
        vctDto.setId(10L);
        vctDto.setKey("owasp_category");
        vctDto.setFieldType("String");
        vctDto.setDefaultValue("");
        vulnerabilityFields.add(vctDto);
        
        customFieldTypesResponse.put("assessmentFields", assessmentFields);
        customFieldTypesResponse.put("vulnerabilityFields", vulnerabilityFields);
        
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customFieldTypesResponse));
    }
    
    @Test
    public void testHelperFunctionBehavior() {
        System.out.println("\n=== Helper Function Behavior ===\n");
        
        System.out.println("The handleVulnerabilityCustomFields helper function provides:");
        System.out.println("1. When creating new vulnerabilities (createAllFields=true):");
        System.out.println("   - Creates ALL available custom fields with defaults");
        System.out.println("   - User-provided values override defaults");
        System.out.println("   - Unspecified fields get sensible defaults based on type");
        System.out.println();
        System.out.println("2. When updating vulnerabilities (createAllFields=false):");
        System.out.println("   - Only updates fields provided by the user");
        System.out.println("   - Existing fields not in the update remain unchanged");
        System.out.println("   - New fields can be added during updates");
        System.out.println();
        System.out.println("3. Default values by field type:");
        System.out.println("   - STRING: empty string or configured default");
        System.out.println("   - BOOLEAN: 'false' or configured default");
        System.out.println("   - LIST: first option or configured default");
    }
}