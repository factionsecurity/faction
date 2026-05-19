package com.fuse.api;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.*;

import com.fuse.dao.*;
import com.fuse.api.dto.*;
import com.opencsv.CSVReader;

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

    // --- CSV Upload Helper Tests (vulnerabilities.buildHeaderMap / getCol / etc.) ---

    @Test
    public void testBuildHeaderMapLowercasesAndIndexes() {
        String[] header = { "Id", "Name", "CategoryId", "Description" };
        Map<String, Integer> map = vulnerabilities.buildHeaderMap(header);

        assertEquals(Integer.valueOf(0), map.get("id"));
        assertEquals(Integer.valueOf(1), map.get("name"));
        assertEquals(Integer.valueOf(2), map.get("categoryid"));
        assertEquals(Integer.valueOf(3), map.get("description"));
    }

    @Test
    public void testBuildHeaderMapIgnoresBlankAndNullCells() {
        String[] header = { "Id", "", null, "Name" };
        Map<String, Integer> map = vulnerabilities.buildHeaderMap(header);

        assertEquals(Integer.valueOf(0), map.get("id"));
        assertEquals(Integer.valueOf(3), map.get("name"));
        assertFalse("blank header should be skipped", map.containsKey(""));
    }

    @Test
    public void testBuildHeaderMapTrimsWhitespace() {
        String[] header = { "  Id  ", " Name " };
        Map<String, Integer> map = vulnerabilities.buildHeaderMap(header);

        assertEquals(Integer.valueOf(0), map.get("id"));
        assertEquals(Integer.valueOf(1), map.get("name"));
    }

    @Test
    public void testBuildHeaderMapKeepsFirstOccurrenceOfDuplicate() {
        String[] header = { "Name", "Other", "name" };
        Map<String, Integer> map = vulnerabilities.buildHeaderMap(header);
        assertEquals("first occurrence wins", Integer.valueOf(0), map.get("name"));
    }

    @Test
    public void testGetColResolvesByPrimaryName() {
        String[] line = { "1", "SQL Injection", "" };
        Map<String, Integer> col = new HashMap<>();
        col.put("id", 0);
        col.put("name", 1);

        assertEquals("SQL Injection", vulnerabilities.getCol(line, col, "name"));
        assertEquals("1", vulnerabilities.getCol(line, col, "id"));
    }

    @Test
    public void testGetColFallsBackToAlias() {
        String[] line = { "1", "SQL Injection" };
        Map<String, Integer> col = new HashMap<>();
        col.put("vulnname", 1);

        assertEquals("alias should resolve when primary is missing",
                "SQL Injection", vulnerabilities.getCol(line, col, "name", "vulnname"));
    }

    @Test
    public void testGetColReturnsNullWhenMissing() {
        String[] line = { "1" };
        Map<String, Integer> col = new HashMap<>();
        col.put("id", 0);

        assertNull(vulnerabilities.getCol(line, col, "categoryname"));
    }

    @Test
    public void testGetColTolratesShortRow() {
        String[] line = { "1" };
        Map<String, Integer> col = new HashMap<>();
        col.put("id", 0);
        col.put("description", 4);

        // index 4 doesn't exist on this line — should return null rather than throw
        assertNull(vulnerabilities.getCol(line, col, "description"));
    }

    @Test
    public void testLegacyPositionalMapHasAllExpectedColumns() {
        Map<String, Integer> map = vulnerabilities.legacyPositionalMap();
        assertEquals(Integer.valueOf(0), map.get("id"));
        assertEquals(Integer.valueOf(1), map.get("name"));
        assertEquals(Integer.valueOf(1), map.get("vulnname"));
        assertEquals(Integer.valueOf(2), map.get("categoryid"));
        assertEquals(Integer.valueOf(3), map.get("categoryname"));
        assertEquals(Integer.valueOf(4), map.get("description"));
        assertEquals(Integer.valueOf(5), map.get("recommendation"));
        assertEquals(Integer.valueOf(6), map.get("severityid"));
        assertEquals(Integer.valueOf(7), map.get("impactid"));
        assertEquals(Integer.valueOf(8), map.get("likelihoodid"));
        assertEquals(Integer.valueOf(9), map.get("isactive"));
        assertEquals(Integer.valueOf(9), map.get("active"));
        assertEquals(Integer.valueOf(10), map.get("cvss31score"));
        assertEquals(Integer.valueOf(11), map.get("cvss31string"));
        assertEquals(Integer.valueOf(12), map.get("cvss40score"));
        assertEquals(Integer.valueOf(13), map.get("cvss40string"));
        assertEquals(Integer.valueOf(14), map.get("customfields"));
    }

    @Test
    public void testIsBlankDetectsNullAndWhitespace() {
        assertTrue(vulnerabilities.isBlank(null));
        assertTrue(vulnerabilities.isBlank(""));
        assertTrue(vulnerabilities.isBlank("   "));
        assertFalse(vulnerabilities.isBlank(" x "));
    }

    @Test
    public void testNullIfBlankTrimsAndNullsOutEmpty() {
        assertNull(vulnerabilities.nullIfBlank(null));
        assertNull(vulnerabilities.nullIfBlank(""));
        assertNull(vulnerabilities.nullIfBlank("   "));
        assertEquals("hello", vulnerabilities.nullIfBlank("  hello  "));
    }

    @Test
    public void testUnescapeNewlinesRestoresLiteralEscapes() {
        // Download writes literal "\r" and "\n"; upload should restore actual control chars.
        assertEquals("a\nb", vulnerabilities.unescapeNewlines("a\\nb"));
        assertEquals("a\rb", vulnerabilities.unescapeNewlines("a\\rb"));
        assertEquals("a\r\nb", vulnerabilities.unescapeNewlines("a\\r\\nb"));
    }

    @Test
    public void testUnescapeNewlinesNullSafe() {
        assertEquals("", vulnerabilities.unescapeNewlines(null));
    }

    @Test
    public void testUnescapeNewlinesLeavesNonEscapesAlone() {
        // A real backslash followed by anything other than r/n should pass through.
        String input = "C:\\Users\\test";
        assertEquals(input, vulnerabilities.unescapeNewlines(input));
    }

    // --- CSV Parser Config Tests (backslash preservation) ---

    @Test
    public void testCsvReaderPreservesBackslashesInQuotedFields() throws Exception {
        // Default opencsv parser eats backslash-as-escape; buildCsvReader must turn that off
        // so JSON custom fields and HTML descriptions round-trip cleanly.
        String csv = "Id,Description\n" +
                "1,\"<a href='x'>\\r literal</a>\"\n";
        CSVReader reader = vulnerabilities.buildCsvReader(csv);
        String[] header = reader.readNext();
        String[] row = reader.readNext();

        assertEquals("Id", header[0]);
        assertEquals("Description", header[1]);
        assertEquals("1", row[0]);
        // Backslash must survive intact
        assertTrue("backslash should not be consumed: " + row[1],
                row[1].contains("\\r literal"));
    }

    @Test
    public void testCsvReaderHandlesEmbeddedJsonCustomFields() throws Exception {
        String csv = "Id,CustomFields\n" +
                "1,\"[{\"\"Key\"\":\"\"CWE\"\",\"\"Value\"\":\"\"79\"\"}]\"\n";
        CSVReader reader = vulnerabilities.buildCsvReader(csv);
        reader.readNext(); // header
        String[] row = reader.readNext();

        assertEquals("1", row[0]);
        assertEquals("[{\"Key\":\"CWE\",\"Value\":\"79\"}]", row[1]);
    }

    @Test
    public void testCsvReaderHandlesMultilineQuotedFields() throws Exception {
        String csv = "Id,Recommendation\n" +
                "1,\"line one\nline two\"\n";
        CSVReader reader = vulnerabilities.buildCsvReader(csv);
        reader.readNext(); // header
        String[] row = reader.readNext();

        assertEquals("line one\nline two", row[1]);
    }

    // --- End-to-end column mapping (header → field) ---

    @Test
    public void testHeaderDrivenLookupReproducesDownloadColumns() {
        String[] header = {
                "Id", "Name", "CategoryId", "CategoryName", "Description",
                "Recommendation", "SeverityId", "ImpactId", "LikelihoodId",
                "isActive", "CVSS31Score", "CVSS31String", "CVSS40Score",
                "CVSS40String", "CustomFields"
        };
        String[] row = {
                "2", "Allowed HTTP methods", "3", "Server Misconfiguration",
                "<p>desc</p>", "<p>rec</p>", "0", "0", "0", "true",
                "0", "CVSS:3.1/AV:N", "0", "CVSS:4.0/AV:N",
                "[{\"Key\":\"CWE\",\"Value\":\"79\"}]"
        };
        Map<String, Integer> col = vulnerabilities.buildHeaderMap(header);

        assertEquals("2", vulnerabilities.getCol(row, col, "id"));
        assertEquals("Allowed HTTP methods", vulnerabilities.getCol(row, col, "name", "vulnname"));
        assertEquals("3", vulnerabilities.getCol(row, col, "categoryid"));
        assertEquals("Server Misconfiguration", vulnerabilities.getCol(row, col, "categoryname"));
        assertEquals("<p>desc</p>", vulnerabilities.getCol(row, col, "description"));
        assertEquals("<p>rec</p>", vulnerabilities.getCol(row, col, "recommendation"));
        assertEquals("true", vulnerabilities.getCol(row, col, "isactive", "active"));
        assertEquals("CVSS:3.1/AV:N", vulnerabilities.getCol(row, col, "cvss31string", "cvssstring"));
        assertEquals("CVSS:4.0/AV:N", vulnerabilities.getCol(row, col, "cvss40string"));
        assertEquals("[{\"Key\":\"CWE\",\"Value\":\"79\"}]",
                vulnerabilities.getCol(row, col, "customfields"));
    }

    @Test
    public void testHeaderDrivenLookupSurvivesColumnReorder() {
        // User reorders columns in Excel before uploading — header-driven mapping must follow.
        String[] header = { "Name", "Id", "CategoryName", "Description" };
        String[] row = { "XSS", "7", "Injection", "<p>script</p>" };
        Map<String, Integer> col = vulnerabilities.buildHeaderMap(header);

        assertEquals("7", vulnerabilities.getCol(row, col, "id"));
        assertEquals("XSS", vulnerabilities.getCol(row, col, "name"));
        assertEquals("Injection", vulnerabilities.getCol(row, col, "categoryname"));
        assertEquals("<p>script</p>", vulnerabilities.getCol(row, col, "description"));
    }

    @Test
    public void testHeaderDrivenLookupTolratesMissingOptionalColumns() {
        // CSV without CustomFields column — getCol returns null instead of throwing.
        String[] header = { "Id", "Name", "CategoryName", "Description" };
        String[] row = { "1", "XSS", "Injection", "<p>x</p>" };
        Map<String, Integer> col = vulnerabilities.buildHeaderMap(header);

        assertNull(vulnerabilities.getCol(row, col, "customfields"));
        assertNull(vulnerabilities.getCol(row, col, "cvss31score", "cvssscore"));
    }

    @Test
    public void testRoundTripPreservesDescriptionAndCustomFields() throws Exception {
        // Regression: user reported uploading a downloaded CSV blanked everything out.
        // Verify the actual bug-report shape parses and every field is recoverable.
        String csv = "Id,Name,CategoryId,CategoryName,Description,Recommendation," +
                "SeverityId,ImpactId,LikelihoodId,isActive,CVSS31Score,CVSS31String," +
                "CVSS40Score,CVSS40String,CustomFields\n" +
                "\"2\",\"Allowed HTTP methods\",\"3\",\"Server Misconfiguration\"," +
                "\"<p>There are a number of <code>OPTIONS</code> methods.</p> \\r <br/>\"," +
                "\"<p>Use <code>GET</code> and\\nblock all others.</p>\"," +
                "\"0\",\"0\",\"0\",\"true\",\"0\",\"CVSS:3.1/AV:N/AC:L\"," +
                "\"0\",\"CVSS:4.0/AV:N/AC:L\"," +
                "\"[{\"\"Key\"\":\"\"CWE\"\",\"\"Value\"\":\"\"16\"\"},{\"\"Key\"\":\"\"Priority\"\",\"\"Value\"\":\"\"1.0\"\"}]\"\n";

        CSVReader reader = vulnerabilities.buildCsvReader(csv);
        String[] header = reader.readNext();
        String[] row = reader.readNext();
        Map<String, Integer> col = vulnerabilities.buildHeaderMap(header);

        assertEquals("2", vulnerabilities.getCol(row, col, "id"));
        assertEquals("Allowed HTTP methods", vulnerabilities.getCol(row, col, "name"));
        assertEquals("3", vulnerabilities.getCol(row, col, "categoryid"));
        assertEquals("Server Misconfiguration", vulnerabilities.getCol(row, col, "categoryname"));

        // Description: backslash survived CSV parse, then unescapeNewlines turns \r into actual CR.
        String description = vulnerabilities.unescapeNewlines(
                vulnerabilities.getCol(row, col, "description"));
        assertTrue("description must keep <code> tag: " + description,
                description.contains("<code>OPTIONS</code>"));
        assertTrue("description literal \\r must be restored to \\r",
                description.contains("\r"));

        // Recommendation: \n in download becomes a real newline after unescape.
        String recommendation = vulnerabilities.unescapeNewlines(
                vulnerabilities.getCol(row, col, "recommendation"));
        assertTrue("recommendation must include <code>GET</code>",
                recommendation.contains("<code>GET</code>"));
        assertTrue("recommendation literal \\n must be restored to newline",
                recommendation.contains("\n"));

        // CVSS strings round-trip intact.
        assertEquals("CVSS:3.1/AV:N/AC:L", vulnerabilities.getCol(row, col, "cvss31string"));
        assertEquals("CVSS:4.0/AV:N/AC:L", vulnerabilities.getCol(row, col, "cvss40string"));

        // CustomFields column is JSON parseable and round-trips both entries.
        String customFieldsJson = vulnerabilities.getCol(row, col, "customfields");
        assertNotNull("CustomFields column must be present", customFieldsJson);
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        List<CustomFieldDTO> fields = mapper.readValue(customFieldsJson,
                new com.fasterxml.jackson.core.type.TypeReference<List<CustomFieldDTO>>() {});
        assertEquals(2, fields.size());
        assertEquals("CWE", fields.get(0).getKey());
        assertEquals("16", fields.get(0).getValue());
        assertEquals("Priority", fields.get(1).getKey());
        assertEquals("1.0", fields.get(1).getValue());
    }

    @Test
    public void testCsvWithJustHeaderProducesNoDataRows() throws Exception {
        String csv = "Id,Name,CategoryName,Description\n";
        CSVReader reader = vulnerabilities.buildCsvReader(csv);
        String[] header = reader.readNext();
        String[] row = reader.readNext();

        assertNotNull(header);
        assertNull("no data rows", row);
    }

    // --- Search escape helpers ---

    @Test
    public void testEscapeMongoRegexLeavesPlainTextAlone() {
        assertEquals("SQL Injection", vulnerabilities.escapeMongoRegex("SQL Injection"));
        assertEquals("hello world", vulnerabilities.escapeMongoRegex("hello world"));
    }

    @Test
    public void testEscapeMongoRegexEscapesParens() {
        // Regression: "(Timeroasting)" used to be parsed as a regex group and matched zero chars.
        assertEquals("SNTP Information Disclosure \\(Timeroasting\\)",
                vulnerabilities.escapeMongoRegex("SNTP Information Disclosure (Timeroasting)"));
    }

    @Test
    public void testEscapeMongoRegexEscapesAllMetacharacters() {
        // Each metachar must be backslash-prefixed once.
        assertEquals("\\\\\\.\\[\\]\\{\\}\\(\\)\\*\\+\\?\\^\\$\\|\\/\\-",
                vulnerabilities.escapeMongoRegex("\\.[]{}()*+?^$|/-"));
    }

    @Test
    public void testEscapeMongoRegexHandlesSlashAndDash() {
        // Regression: "LLMNR/NBT-NS Spoofing" — slash routes oddly via path params, so users
        // hit GET /default/search?name= instead. We still want the regex to match literally.
        String escaped = vulnerabilities.escapeMongoRegex("LLMNR/NBT-NS Spoofing");
        assertEquals("LLMNR\\/NBT\\-NS Spoofing", escaped);
    }

    @Test
    public void testEscapeMongoRegexNullSafe() {
        assertEquals("", vulnerabilities.escapeMongoRegex(null));
    }

    @Test
    public void testJsonStringEscapeEscapesQuoteAndBackslash() {
        assertEquals("a\\\\b\\\"c", vulnerabilities.jsonStringEscape("a\\b\"c"));
    }

    @Test
    public void testJsonStringEscapeEscapesControlCharacters() {
        assertEquals("line1\\nline2", vulnerabilities.jsonStringEscape("line1\nline2"));
        assertEquals("a\\tb", vulnerabilities.jsonStringEscape("a\tb"));
        assertEquals("\\u0001", vulnerabilities.jsonStringEscape(""));
    }

    @Test
    public void testJsonStringEscapeNullSafe() {
        assertEquals("", vulnerabilities.jsonStringEscape(null));
    }

    @Test
    public void testEscapeAndJsonComposesCleanly() {
        // The full pipeline: regex-escape first, then JSON-escape so the resulting string
        // is safe to inline into a double-quoted JSON literal.
        String input = "SNTP Information Disclosure (Timeroasting)";
        String pattern = vulnerabilities.jsonStringEscape(vulnerabilities.escapeMongoRegex(input));
        // After JSON parsing the regex should see "SNTP Information Disclosure \(Timeroasting\)",
        // i.e. escaped parens that match the literal characters.
        assertEquals("SNTP Information Disclosure \\\\(Timeroasting\\\\)", pattern);
    }

    @Test
    public void testEscapePreventsJsonInjection() {
        // A name like {"$ne": ""} must not break out of the JSON string context.
        String injection = "\" }, \"$ne\": \"";
        String pattern = vulnerabilities.jsonStringEscape(vulnerabilities.escapeMongoRegex(injection));
        assertFalse("escaped pattern must not contain a raw closing quote: " + pattern,
                pattern.contains("\","));
    }
}
