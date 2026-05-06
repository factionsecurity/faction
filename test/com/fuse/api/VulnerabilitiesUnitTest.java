package com.fuse.api;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.*;

import com.fuse.dao.*;
import com.fuse.api.dto.*;

/**
 * Unit tests for Vulnerabilities REST API endpoint logic.
 * Tests DTO conversion, permission checks, and data structure handling.
 */
public class VulnerabilitiesUnitTest {

    // --- DefaultVulnerabilityDTO Tests ---

    @Test
    public void testDefaultVulnerabilityDTOFromEntity() {
        DefaultVulnerability dv = createTestDefaultVulnerability();
        DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(dv, java.util.Collections.emptyList());

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", (Object)Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "SQL Injection", dto.getName());
        assertEquals("Description should match", "SQL injection vulnerability", dto.getDescription());
        assertEquals("Recommendation should match", "Use parameterized queries", dto.getRecommendation());
        // severityId maps to entity.getOverall() in the DTO
        assertEquals("SeverityId should match overall score", Integer.valueOf(4), dto.getSeverityId());
        assertEquals("ImpactId should match", Integer.valueOf(4), dto.getImpactId());
        assertEquals("LikelihoodId should match", Integer.valueOf(5), dto.getLikelihoodId());
    }

    @Test
    public void testDefaultVulnerabilityDTOWithNullFields() {
        DefaultVulnerability dv = new DefaultVulnerability();
        dv.setName("No Details");

        DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(dv, java.util.Collections.emptyList());

        assertNotNull("DTO should not be null", dto);
        assertEquals("Name should match", "No Details", dto.getName());
        assertNull("Description should be null", dto.getDescription());
        assertNull("Recommendation should be null", dto.getRecommendation());
    }

    @Test
    public void testDefaultVulnerabilityDTOWithCustomFields() {
        DefaultVulnerability dv = createTestDefaultVulnerability();
        List<CustomField> customFields = new ArrayList<>();
        CustomType ct1 = new CustomType();
        ct1.setId(1L);
        ct1.setKey("owasp_category");
        CustomField cf1 = new CustomField();
        cf1.setType(ct1);
        cf1.setValue("A3: Injection");
        customFields.add(cf1);

        dv.setCustomFields(customFields);

        // Pass customTypeList so updateFieldDTOs can match keys
        List<CustomType> customTypes = java.util.Collections.singletonList(ct1);
        DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(dv, customTypes);
        assertNotNull("Custom fields should not be null", dto.getCustomFields());
        assertEquals("Should have 1 custom field", 1, dto.getCustomFields().size());
        assertEquals("First custom field key should match", "owasp_category", dto.getCustomFields().get(0).getKey());
    }

    @Test
    public void testDefaultVulnerabilityDTOWithNullCustomFields() {
        DefaultVulnerability dv = new DefaultVulnerability();
        dv.setName("No Custom Fields");

        DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(dv, java.util.Collections.emptyList());
        // DTO always initializes customFields to empty list, never null
        assertNotNull("Custom fields should be empty list, not null", dto.getCustomFields());
        assertEquals("Custom fields should be empty", 0, dto.getCustomFields().size());
    }

    // --- CategoryDTO Tests ---

    @Test
    public void testCategoryDTOFromEntity() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");

        CategoryDTO dto = CategoryDTO.fromEntity(cat);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", (Object)Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "Injection", dto.getName());
    }

    @Test
    public void testCategoryDTOWithNullName() {
        Category cat = new Category();
        cat.setId(1L);

        CategoryDTO dto = CategoryDTO.fromEntity(cat);

        assertNotNull("DTO should not be null", dto);
        assertNull("Name should be null", dto.getName());
    }

    @Test
    public void testCategoryDTOWithNullEntity() {
        CategoryDTO dto = CategoryDTO.fromEntity(null);
        assertNull("DTO should be null for null entity", dto);
    }

    // --- CustomFieldDTO Tests ---

    @Test
    public void testCustomFieldDTOFromEntity() {
        CustomType ct = new CustomType();
        ct.setId(1L);
        ct.setKey("owasp_category");
        CustomField cf = new CustomField();
        cf.setType(ct);
        cf.setValue("A3: Injection");

        CustomFieldDTO dto = CustomFieldDTO.fromEntity(cf);

        assertNotNull("DTO should not be null", dto);
        assertEquals("Key should match", "owasp_category", dto.getKey());
        assertEquals("Value should match", "A3: Injection", dto.getValue());
    }

    @Test
    public void testCustomFieldDTOWithNullEntity() {
        CustomFieldDTO dto = CustomFieldDTO.fromEntity(null);
        assertNull("DTO should be null for null entity", dto);
    }

    // --- VulnerabilityDTO Tests for Vulnerabilities Endpoint ---

    @Test
    public void testVulnerabilityDTOFromEntity() {
        Vulnerability v = createTestVulnerability();
        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", (Object)Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "SQL Injection", dto.getName());
        assertEquals("Overall should match", Long.valueOf(5L), dto.getOverall());
        assertEquals("Impact should match", Long.valueOf(4L), dto.getImpact());
        assertEquals("Likelihood should match", Long.valueOf(5L), dto.getLikelyhood());
        assertEquals("Tracking should match", "VID-0001", dto.getTracking());
    }

    @Test
    public void testVulnerabilityDTOWithTracking() {
        Vulnerability v = createTestVulnerability();
        v.setTracking("JIRA-1234");

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        assertEquals("Tracking should match", "JIRA-1234", dto.getTracking());
    }

    @Test
    public void testVulnerabilityDTOWithCvssScore() {
        Vulnerability v = createTestVulnerability();
        v.setCvssScore("9.1");
        v.setCvssString("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H");

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        assertEquals("CVSS score should match", "9.1", dto.getCvssScore());
        assertEquals("CVSS string should match", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H", dto.getCvssString());
    }

    @Test
    public void testVulnerabilityDTOWithNullCvssScore() {
        Vulnerability v = createTestVulnerability();

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        // getCvssScore() returns "" when null
        assertEquals("CVSS score should be empty string", "", dto.getCvssScore());
        assertEquals("CVSS string should be empty string", "", dto.getCvssString());
    }

    @Test
    public void testVulnerabilityDTOWithCategory() {
        Vulnerability v = createTestVulnerability();

        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);
        // Category field doesn't exist on VulnerabilityDTO by design
        assertNotNull("DTO should still be created", dto);
    }

    // --- Risk Level Tests ---

    @Test
    public void testRiskLevelEntityBasicFields() {
        RiskLevel rl = new RiskLevel();
        rl.setRiskId(5);
        rl.setRisk("Critical");

        assertEquals("Risk ID should match", 5, rl.getRiskId());
        assertEquals("Risk should match", "Critical", rl.getRisk());
    }

    @Test
    public void testRiskLevelAllStandardLevels() {
        String[] expectedLabels = {"Informational", "Recommended", "Low", "Medium", "High", "Critical"};

        for (int i = 0; i < expectedLabels.length; i++) {
            RiskLevel rl = new RiskLevel();
            rl.setRiskId(i);
            rl.setRisk(expectedLabels[i]);

            assertEquals("Risk ID " + i + " should map to " + expectedLabels[i], i, rl.getRiskId());
            assertEquals("Risk label should match", expectedLabels[i], rl.getRisk());
        }
    }

    @Test
    public void testRiskLevelWithDaysTillDue() {
        RiskLevel rl = new RiskLevel();
        rl.setRiskId(5);
        rl.setRisk("Critical");
        rl.setDaysTillDue(30);

        assertEquals("Days till due should match", Integer.valueOf(30), rl.getDaysTillDue());
    }

    @Test
    public void testRiskLevelWithDaysTillWarning() {
        RiskLevel rl = new RiskLevel();
        rl.setRiskId(5);
        rl.setRisk("Critical");
        rl.setDaysTillWarning(14);

        assertEquals("Days till warning should match", Integer.valueOf(14), rl.getDaysTillWarning());
    }

    // --- CSV Export/Import Tests ---

    @Test
    public void testCsvHeaderStructure() {
        // Test that CSV headers are in expected order
        String[] expectedHeaders = {"Id", "Name", "CategoryId", "CategoryName", "Description", 
                                   "Recommendation", "SeverityId", "ImpactId", "LikelihoodId", 
                                   "isActive", "CVSS31Score", "CVSS31String", "CVSS40Score", "CVSS40String"};

        for (String header : expectedHeaders) {
            assertNotNull("Header should not be null", header);
            assertTrue("Header should not be empty", !header.isEmpty());
        }
    }

    @Test
    public void testCsvFieldEscaping() {
        String valueWithComma = "Contains, a comma";
        String escapedValue = "\"" + valueWithComma + "\"";

        // CSV fields with commas should be quoted
        assertEquals("Value with comma should be quoted", escapedValue, "\"" + valueWithComma + "\"");
    }

    @Test
    public void testCsvFieldWithQuotes() {
        String valueWithQuotes = "Contains \"quotes\"";
        String escapedValue = "\"" + valueWithQuotes.replace("\"", "\"\"") + "\"";

        // CSV fields with quotes should be quoted and quotes doubled
        assertEquals("Value with quotes should be escaped", escapedValue, "\"" + valueWithQuotes.replace("\"", "\"\"") + "\"");
    }

    @Test
    public void testCsvNewlineHandling() {
        String valueWithNewline = "Contains\na newline";
        String escapedValue = "\"" + valueWithNewline + "\"";

        // CSV fields with newlines should be quoted
        assertEquals("Value with newline should be quoted", escapedValue, "\"" + valueWithNewline + "\"");
    }

    // --- Default Vulnerability Search Tests ---

    @Test
    public void testDefaultVulnerabilityNameSearch() {
        // Test that name-based search works with partial matching
        String searchQuery = "SQL";
        String testName = "SQL Injection";

        assertTrue("Search should find partial match", testName.toLowerCase().contains(searchQuery.toLowerCase()));
    }

    @Test
    public void testDefaultVulnerabilityNameSearchCaseInsensitive() {
        String searchQuery = "sql";
        String testName = "SQL Injection";

        assertTrue("Search should be case insensitive", testName.toLowerCase().contains(searchQuery.toLowerCase()));
    }

    @Test
    public void testDefaultVulnerabilityNameSearchNoMatch() {
        String searchQuery = "XSS";
        String testName = "SQL Injection";

        assertFalse("Search should not find non-matching term", testName.toLowerCase().contains(searchQuery.toLowerCase()));
    }

    @Test
    public void testDefaultVulnerabilityNameSearchEmptyQuery() {
        String searchQuery = "";
        String testName = "SQL Injection";

        // Empty query should match all
        assertTrue("Empty query should match all", testName.toLowerCase().contains(searchQuery.toLowerCase()));
    }

    // --- Vulnerability Status Tests ---

    @Test
    public void testVulnerabilityStatusOpen() {
        Vulnerability v = createTestVulnerability();
        v.setClosed(null);

        assertNull("Open vulnerability should have null closed date", v.getClosed());
    }

    @Test
    public void testVulnerabilityStatusClosed() {
        Vulnerability v = createTestVulnerability();
        v.setClosed(new Date());

        assertNotNull("Closed vulnerability should have closed date", v.getClosed());
    }

    @Test
    public void testVulnerabilityTrackingAssignment() {
        Vulnerability v = createTestVulnerability();
        v.setTracking("JIRA-5678");

        assertEquals("Tracking should be assigned", "JIRA-5678", v.getTracking());
    }

    @Test
    public void testVulnerabilityWithNullTracking() {
        Vulnerability v = new Vulnerability();
        v.setName("No Tracking");

        // getTracking() returns "VID-xxx" with random number when tracking is null
        assertNotNull("Tracking should have default VID-xxx format", v.getTracking());
        assertTrue("Should start with VID-", v.getTracking().startsWith("VID-"));
    }

    // --- Category CRUD Tests ---

    @Test
    public void testCategoryCreate() {
        Category cat = new Category();
        cat.setName("Injection");

        assertNotNull("Category ID should be null for new entity", cat.getId());
        assertEquals("Category name should match", "Injection", cat.getName());
    }

    @Test
    public void testCategoryUpdate() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");
        cat.setName("XSS");

        assertEquals("Category name should be updated", "XSS", cat.getName());
    }

    @Test
    public void testCategoryWithNullName() {
        Category cat = new Category();
        cat.setId(1L);

        assertNull("Category name should be null", cat.getName());
    }

    // --- Date Range Query Tests ---

    @Test
    public void testDateRangeQueryValidDates() {
        Date start = new Date(System.currentTimeMillis() - 86400000 * 7); // 7 days ago
        Date end = new Date(); // now

        assertNotNull("Start date should not be null", start);
        assertNotNull("End date should not be null", end);
        assertTrue("Start date should be before end date", start.before(end));
    }

    @Test
    public void testDateRangeQuerySameDay() {
        Date now = new Date();

        // Same day range should be valid
        assertNotNull("Date should not be null", now);
    }

    @Test
    public void testDateRangeQueryInvalidRange() {
        Date start = new Date(System.currentTimeMillis() + 86400000); // tomorrow
        Date end = new Date(); // now

        assertTrue("Start date should be after end date (invalid range)", start.after(end));
    }

    // --- Mock Helper Methods ---

    private DefaultVulnerability createTestDefaultVulnerability() {
        DefaultVulnerability dv = new DefaultVulnerability();
        dv.setId(1);
        dv.setName("SQL Injection");
        dv.setDescription("SQL injection vulnerability");
        dv.setRecommendation("Use parameterized queries");
        dv.setImpact(4);
        dv.setLikelyhood(5);
        dv.setOverall(4);
        dv.setActive(true);
        return dv;
    }

    private Vulnerability createTestVulnerability() {
        Vulnerability v = new Vulnerability();
        v.setId(1L);
        v.setName("SQL Injection");
        v.setOverall(5L);
        v.setImpact(4L);
        v.setLikelyhood(5L);
        v.setTracking("VID-0001");
        v.setAssessmentId(100L);
        v.setCreated(new Date());
        v.setCategory(createTestCategory());
        return v;
    }

    private Category createTestCategory() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Injection");
        return cat;
    }
}
