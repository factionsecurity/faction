package com.fuse.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.AssessmentDTO;
import com.fuse.api.dto.VulnerabilityDTO;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.Note;
import com.fuse.dao.Permissions;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Comprehensive DTO tests covering serialization, field mapping, and edge cases.
 */
public class DtoSerializationTest extends MongoTestBase {

    private ObjectMapper mapper;
    private User testUser;
    private Assessment testAssessment;
    private Vulnerability testVuln;
    private List<RiskLevel> riskLevels;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        setupTestEntities();
    }

    private void setupTestEntities() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFname("Test");
        testUser.setLname("User");
        testUser.setEmail("test@example.com");

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
        notebook.add(new Note());
        notebook.get(0).setNote("Test notes");
        testAssessment.setNotebook(notebook);

        Campaign camp = new Campaign();
        camp.setId(1L);
        camp.setName("Q1 Testing");
        testAssessment.setCampaign(camp);

        riskLevels = new ArrayList<>();
        String[] labels = {"Informational", "Recommended", "Low", "Medium", "High", "Critical"};
        for (int i = 0; i < labels.length; i++) {
            RiskLevel rl = new RiskLevel();
            rl.setRiskId(i);
            rl.setRisk(labels[i]);
            riskLevels.add(rl);
        }

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");

        testVuln = new Vulnerability();
        testVuln.setId(1L);
        testVuln.setName("SQL Injection");
        testVuln.setOverall(5L);
        testVuln.setImpact(4L);
        testVuln.setLikelyhood(3L);
        testVuln.setTracking("VID-0001");
        testVuln.setCategory(cat);
        testVuln.setAssessmentId(100L);
        testVuln.setCreated(new Date());
        testVuln.setLevels(riskLevels);
        testVuln.setDescription("<p>SQL injection vulnerability</p>");
        testVuln.setRecommendation("<p>Use parameterized queries</p>");
        testVuln.setDetails("<p>Exploit steps</p>");
    }

    // --- AssessmentDTO Tests ---

    @Test
    public void testAssessmentDTOFromEntity() {
        AssessmentDTO dto = AssessmentDTO.fromEntity(testAssessment);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(100L), dto.getId());
        assertEquals("Name should match", "Test Assessment", dto.getName());
        assertEquals("App ID should match", "APP-123", dto.getAppId());
        assertEquals("Type should match", "Web Application", dto.getType());
        assertEquals("Status should match", "In Progress", dto.getStatus());
        assertEquals("Campaign should match", "Q1 Testing", dto.getCampaign());
    }

    @Test
    public void testAssessmentDTOWithNullAssessors() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Assessors");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        notebook.add(new Note());
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        assertNull("Assessors should be null", dto.getAssessors());
    }

    @Test
    public void testAssessmentDTOWithNullCampaign() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Campaign");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        notebook.add(new Note());
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        assertNull("Campaign should be null", dto.getCampaign());
        assertNull("Campaign ID should be null", dto.getCampaignId());
    }

    @Test
    public void testAssessmentDTOWithNullType() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Type");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        notebook.add(new Note());
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        assertNull("Type should be null", dto.getType());
        assertNull("Type ID should be null", dto.getTypeId());
    }

    @Test
    public void testAssessmentDTOWithNullVulnerabilities() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Vulns");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        notebook.add(new Note());
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        assertEquals("Vulnerability count should be 0", Integer.valueOf(0), dto.getVulnerabilityCount());
    }

    @Test
    public void testAssessmentDTOWithNullEngagement() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Engagement");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        notebook.add(new Note());
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        // Engagement contact is created even if null - UserDTO handles null
        assertNotNull("Engagement contact DTO should exist", dto.getEngagementContact());
    }

    @Test
    public void testAssessmentDTOWithNotes() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("With Notes");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        Note note = new Note();
        note.setNote("Important notes");
        notebook.add(note);
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        assertEquals("Notes should match", "Important notes", dto.getNotes());
    }

    @Test
    public void testAssessmentDTOWithEmptyNotes() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("Empty Notes");
        a.setStart(new Date());
        a.setEnd(new Date());
        List<Note> notebook = new ArrayList<>();
        Note note = new Note();
        note.setNote("");
        notebook.add(note);
        a.setNotebook(notebook);

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        assertEquals("Notes should be empty", "", dto.getNotes());
    }

    @Test
    public void testAssessmentDTOWithNullNotebook() {
        Assessment a = new Assessment();
        a.setId(1L);
        a.setName("No Notebook");
        a.setStart(new Date());
        a.setEnd(new Date());

        AssessmentDTO dto = AssessmentDTO.fromEntity(a);
        // Notes should be empty when notebook is null
        assertEquals("Notes should be empty", "", dto.getNotes());
    }

    @Test
    public void testAssessmentDTOSerialization() throws Exception {
        AssessmentDTO dto = AssessmentDTO.fromEntity(testAssessment);
        String json = mapper.writeValueAsString(dto);

        assertTrue("JSON should contain Id", json.contains("\"Id\":100"));
        assertTrue("JSON should contain Name", json.contains("\"Name\":\"Test Assessment\""));
        assertTrue("JSON should contain Type", json.contains("\"Type\":\"Web Application\""));
        assertTrue("JSON should contain Campaign", json.contains("\"Campaign\":\"Q1 Testing\""));
        assertTrue("JSON should contain Assessors", json.contains("\"Assessors\""));
    }

    // --- VulnerabilityDTO Tests ---

    @Test
    public void testVulnerabilityDTOFromEntity() {
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(testVuln);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "SQL Injection", dto.getName());
        assertEquals("Overall should match", Long.valueOf(5L), dto.getOverall());
        assertEquals("Impact should match", Long.valueOf(4L), dto.getImpact());
        assertEquals("Likelihood should match", Long.valueOf(3L), dto.getLikelyhood());
        assertEquals("Tracking should match", "VID-0001", dto.getTracking());
    }

    @Test
    public void testVulnerabilityDTOIncludesLargeBlobs() {
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(testVuln);

        // VulnerabilityDTO should include description, recommendation, details
        assertEquals("Description should be present", "<p>SQL injection vulnerability</p>", dto.getDescription());
        assertEquals("Recommendation should be present", "<p>Use parameterized queries</p>", dto.getRecommendation());
        assertEquals("Details should be present", "<p>Exploit steps</p>", dto.getDetails());
    }

    @Test
    public void testVulnerabilityDTOWithNullDescription() {
        Vulnerability v = new Vulnerability();
        v.setName("No Description");
        v.setAssessmentId(100L);
        // Set ID to avoid NPE from @TableGenerator initialization
        try {
            java.lang.reflect.Field idField = Vulnerability.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(v, 1L);
        } catch (Exception e) {
            // Ignore
        }

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        // Vulnerability.getDescription() returns "" when null
        assertEquals("Description should be empty string when entity has null", "", dto.getDescription());
    }

    @Test
    public void testVulnerabilityDTOWithNullTracking() {
        Vulnerability v = new Vulnerability();
        v.setName("No Tracking");
        v.setAssessmentId(100L);
        try {
            java.lang.reflect.Field idField = Vulnerability.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(v, 1L);
        } catch (Exception e) {
            // Ignore
        }

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        // Vulnerability has a default tracking value, so DTO will have that value
        assertNotNull("Tracking should have default value from entity", dto.getTracking());
        assertTrue("Tracking should start with VID-", dto.getTracking().startsWith("VID-"));
    }

    @Test
    public void testVulnerabilityDTOWithNullOverall() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Overall");
        v.setAssessmentId(100L);

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        assertNull("Overall should be null", dto.getOverall());
        assertEquals("OverallStr should be Info", "Info", dto.getOverallStr());
    }

    @Test
    public void testVulnerabilityDTOWithNullImpact() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Impact");
        v.setAssessmentId(100L);

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        assertNull("Impact should be null", dto.getImpact());
        assertEquals("ImpactStr should be Info", "Info", dto.getImpactStr());
    }

    @Test
    public void testVulnerabilityDTOWithNullLikelyhood() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Likelyhood");
        v.setAssessmentId(100L);

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        assertNull("Likelyhood should be null", dto.getLikelyhood());
        assertEquals("LikelyhoodStr should be Info", "Info", dto.getLikelyhoodStr());
    }

    @Test
    public void testVulnerabilityDTOWithNullCategory() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("No Category");
        v.setAssessmentId(100L);

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        // Category field doesn't exist on VulnerabilityDTO by design
        assertNotNull("DTO should still be created", dto);
    }

    @Test
    public void testVulnerabilityDTOWithNullCvssScore() {
        Vulnerability v = new Vulnerability();
        v.setName("No CVSS");
        v.setAssessmentId(100L);
        try {
            java.lang.reflect.Field idField = Vulnerability.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(v, 1L);
        } catch (Exception e) {
            // Ignore
        }

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        // Vulnerability.getCvssScore() returns "" when null
        assertEquals("CVSS score should be empty string when entity has null", "", dto.getCvssScore());
        assertEquals("CVSS string should be empty string when entity has null", "", dto.getCvssString());
    }

    @Test
    public void testVulnerabilityDTOSerialization() throws Exception {
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(testVuln);
        String json = mapper.writeValueAsString(dto);

        assertTrue("JSON should contain Id", json.contains("\"Id\":1"));
        assertTrue("JSON should contain Name", json.contains("\"Name\":\"SQL Injection\""));
        assertTrue("JSON should contain Overall", json.contains("\"Overall\":5"));
        assertTrue("JSON should contain Impact", json.contains("\"Impact\":4"));
        assertTrue("JSON should contain Likelyhood", json.contains("\"Likelyhood\":3"));
        assertTrue("JSON should contain Tracking", json.contains("\"Tracking\":\"VID-0001\""));
        assertTrue("JSON should contain Description", json.contains("\"Description\""));
        assertTrue("JSON should contain Recommendation", json.contains("\"Recommendation\""));
        assertTrue("JSON should contain Details", json.contains("\"Details\""));
    }

    // --- Edge Cases ---

    @Test
    public void testFromEntityWithNullAssessment() {
        AssessmentDTO dto = AssessmentDTO.fromEntity(null);
        assertNull("DTO should be null for null assessment", dto);
    }

    @Test
    public void testFromEntityWithNullVulnerability() {
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(null);
        assertNull("DTO should be null for null vulnerability", dto);
    }

    @Test
    public void testMapSerialization() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put("Critical", 3);
        map.put("High", 5);
        map.put("Medium", 2);

        String json = mapper.writeValueAsString(map);
        Map<String, Integer> parsed = mapper.readValue(json, new TypeReference<Map<String, Integer>>() {});

        assertEquals("Parsed map should have same size", 3, parsed.size());
        assertEquals("Critical should match", Integer.valueOf(3), parsed.get("Critical"));
        assertEquals("High should match", Integer.valueOf(5), parsed.get("High"));
        assertEquals("Medium should match", Integer.valueOf(2), parsed.get("Medium"));
    }

    @Test
    public void testErrorResponseFormat() throws Exception {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Authorized");
        error.put("details", "Invalid API key");

        String json = mapper.writeValueAsString(error);
        Map<String, String> parsed = mapper.readValue(json, new TypeReference<Map<String, String>>() {});

        assertEquals("Error should match", "Not Authorized", parsed.get("error"));
        assertEquals("Details should match", "Invalid API key", parsed.get("details"));
    }

    @Test
    public void testSuccessResponseFormat() throws Exception {
        Map<String, Object> success = new HashMap<>();
        success.put("success", true);
        success.put("vid", 123L);

        String json = mapper.writeValueAsString(success);
        Map<String, Object> parsed = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        assertEquals("Success should match", Boolean.TRUE, parsed.get("success"));
        // Jackson deserializes JSON numbers as Integer by default
        assertEquals("VID should match", Integer.valueOf(123), parsed.get("vid"));
    }

    @Test
    public void testBase64Encoding() {
        String original = "SQL injection vulnerability";
        String base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes());

        assertEquals("Base64 should match", "U1FMIGluamVjdGlvbiB2dWxuZXJhYmlsaXR5", base64);

        String decoded = new String(java.util.Base64.getDecoder().decode(base64));
        assertEquals("Decoded should match original", original, decoded);
    }

    @Test
    public void testJsonProcessingExceptionHandling() {
        // Verify ObjectMapper handles complex objects
        try {
            String json = mapper.writeValueAsString(testAssessment);
            assertNotNull("JSON should not be null", json);
        } catch (JsonProcessingException e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSpecialCharactersInStrings() {
        testUser.setFname("Test & User");
        testUser.setLname("O'Brien");
        testUser.setEmail("test+tag@example.com");

        AssessmentDTO userDto = new AssessmentDTO();
        userDto.setEngagementContact(new AssessmentDTO.UserDTO(testUser));

        try {
            String json = mapper.writeValueAsString(userDto);
            assertTrue("JSON should contain special chars", json.contains("Test & User"));
        } catch (JsonProcessingException e) {
            fail("Should handle special characters: " + e.getMessage());
        }
    }

    @Test
    public void testEmptyStringHandling() {
        testUser.setFname("");
        testUser.setLname("");
        testUser.setEmail("");

        AssessmentDTO.UserDTO userDto = new AssessmentDTO.UserDTO(testUser);

        assertEquals("Empty first name should be empty string", "", userDto.getFirstName());
        assertEquals("Empty last name should be empty string", "", userDto.getLastName());
        assertEquals("Empty email should be empty string", "", userDto.getEmail());
    }

    @Test
    public void testNullStringHandling() {
        testUser.setFname(null);
        testUser.setLname(null);
        testUser.setEmail(null);

        AssessmentDTO.UserDTO userDto = new AssessmentDTO.UserDTO(testUser);

        assertEquals("Null first name should be null", null, userDto.getFirstName());
        assertEquals("Null last name should be null", null, userDto.getLastName());
        assertEquals("Null email should be null", null, userDto.getEmail());
    }
}
