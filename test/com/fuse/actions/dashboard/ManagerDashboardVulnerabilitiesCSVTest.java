package com.fuse.actions.dashboard;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.struts2.junit.StrutsJUnit4TestCase;
import org.junit.Before;
import org.junit.Test;

import com.fuse.dao.*;

/**
 * Tests for ManagerDashboardVulnerabilitiesCSV action - Vulnerability CSV Export
 * Tests authentication, authorization, date filtering, and CSV content generation
 */
public class ManagerDashboardVulnerabilitiesCSVTest extends StrutsJUnit4TestCase<ManagerDashboardVulnerabilitiesCSV> {

    private EntityManager mockEm;
    private User managerUser;
    private User assessorUser;
    private User noPermUser;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupTestUsers();
    }

    private void setupTestUsers() {
        // Manager user (has manager permission)
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setUsername("manager");
        managerUser.setFname("Manager");
        managerUser.setLname("User");
        Permissions managerPerms = new Permissions();
        managerPerms.setManager(true);
        managerPerms.setAssessor(true);
        managerUser.setPermissions(managerPerms);

        Teams team = new Teams();
        team.setId(1L);
        team.setTeamName("Security Team");
        managerUser.setTeam(team);

        // Assessor user (no manager permission)
        assessorUser = new User();
        assessorUser.setId(2L);
        assessorUser.setUsername("assessor");
        assessorUser.setFname("Assessor");
        assessorUser.setLname("User");
        Permissions assessorPerms = new Permissions();
        assessorPerms.setManager(false);
        assessorPerms.setAssessor(true);
        assessorUser.setPermissions(assessorPerms);
        assessorUser.setTeam(team);

        // No permission user
        noPermUser = new User();
        noPermUser.setId(3L);
        noPermUser.setUsername("noperm");
        noPermUser.setFname("No");
        noPermUser.setLname("Permission");
        Permissions noPerms = new Permissions();
        noPerms.setManager(false);
        noPerms.setAssessor(false);
        noPermUser.setPermissions(noPerms);
    }

    private Object getField(ManagerDashboardVulnerabilitiesCSV action, String fieldName) throws Exception {
        Class<?> clazz = ManagerDashboardVulnerabilitiesCSV.class;
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(action);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private void setField(ManagerDashboardVulnerabilitiesCSV action, String fieldName, Object value) throws Exception {
        Class<?> clazz = ManagerDashboardVulnerabilitiesCSV.class;
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(action, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    // --- Authentication & Authorization Tests ---

    @Test
    public void testActionRequiresManagerRole() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        
        // Don't set manager role in ActionContext (simulates non-manager)
        
        String result = action.exportCSV();
        
        assertEquals("Should return LOGIN for non-manager user", "login", result);
        assertNull("Should not generate CSV for non-manager", action.getInputStream());
    }

    @Test
    public void testActionAllowsManagerAccess() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        
        // Mock the EntityManager to avoid database calls
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        mockAllRequiredQueries(new ArrayList<Assessment>());
        
        String result = action.exportCSV();
        
        assertEquals("Should return SUCCESS for manager user", "success", result);
        assertNotNull("Should generate CSV for manager", action.getInputStream());
        assertNotNull("Should set filename", action.getFilename());
    }

    @Test
    public void testActionRequiresLogin() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        
        // No manager role set (not logged in or no permissions)
        
        String result = action.exportCSV();
        
        assertEquals("Should return LOGIN for unauthenticated user", "login", result);
    }

    @Test
    public void testNonManagerAssessorDenied() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        
        // Assessor without manager permission (no isManager in ActionContext)
        
        String result = action.exportCSV();
        
        assertEquals("Assessor without manager permission should be denied", "login", result);
        assertNull("Should not generate CSV", action.getInputStream());
    }

    @Test
    public void testUserWithNoPermissionsDenied() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        
        // User with no permissions (no isManager in ActionContext)
        
        String result = action.exportCSV();
        
        assertEquals("User with no permissions should be denied", "login", result);
        assertNull("Should not generate CSV", action.getInputStream());
    }

    // --- CSV Content Tests ---

    @Test
    public void testCSVHeaderGeneration() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Set up vulnerability custom types
        CustomType customField1 = new CustomType();
        customField1.setId(1L);
        customField1.setKey("Affected Component");
        customField1.setType(1); // VULN type
        customField1.setFieldType(0);
        customField1.setDeleted(false);
        
        List<CustomType> customTypes = Arrays.asList(customField1);
        
        mockAllRequiredQueriesWithCustomTypes(new ArrayList<Assessment>(), customTypes);
        
        String result = action.exportCSV();
        
        assertEquals("Should return SUCCESS", "success", result);
        
        // Read CSV content
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String header = reader.readLine();
        
        // Verify header contains all expected columns
        assertTrue("Header should contain Vulnerability ID", header.contains("Vulnerability ID"));
        assertTrue("Header should contain Vulnerability Name", header.contains("Vulnerability Name"));
        assertTrue("Header should contain Assessment Name", header.contains("Assessment Name"));
        assertTrue("Header should contain Assessment AppId", header.contains("Assessment AppId"));
        assertTrue("Header should contain Severity", header.contains("Severity"));
        assertTrue("Header should contain CVSS Score", header.contains("CVSS Score"));
        assertTrue("Header should contain Category", header.contains("Category"));
        assertTrue("Header should contain Opened Date", header.contains("Opened Date"));
        assertTrue("Header should contain Closed Date", header.contains("Closed Date"));
        assertTrue("Header should contain Status", header.contains("Status"));
        assertTrue("Header should contain Tracking ID", header.contains("Tracking ID"));
        assertTrue("Header should contain custom field", header.contains("Affected Component"));
        
        reader.close();
    }

    @Test
    public void testAssessmentInfoColumnsIncluded() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);

        // Assessment custom field (type 0 = ASMT, fieldType 0 = STRING)
        CustomType asmtField = new CustomType();
        asmtField.setId(10L);
        asmtField.setKey("Business Unit");
        asmtField.setType(0);
        asmtField.setFieldType(0);
        asmtField.setDeleted(false);

        Assessment testAsmt = createTestAssessment();
        CustomField cf = new CustomField();
        cf.setType(asmtField);
        cf.setValue("Finance");
        testAsmt.setCustomFields(Arrays.asList(cf));

        mockAllRequiredQueries(Arrays.asList(testAsmt));
        // Assessment custom types (type = 0) return the field; vuln types (type = 1) empty
        doReturn(mockTypedQuery(Arrays.asList(asmtField)))
            .when(mockEm).createQuery(contains("type = 0"), eq(CustomType.class));
        doReturn(mockTypedQuery(new ArrayList<CustomType>()))
            .when(mockEm).createQuery(contains("type = 1"), eq(CustomType.class));

        List<Vulnerability> vulns = createTestVulnerabilities();
        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        doReturn(vulnQuery).when(vulnQuery).setParameter(eq("aid"), any());
        doReturn(vulns).when(vulnQuery).getResultList();
        doReturn(vulnQuery).when(mockEm).createQuery(contains("Vulnerability"), eq(Vulnerability.class));

        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String header = reader.readLine();
        String dataRow = reader.readLine();

        // Header includes the assessment id and the assessment custom field
        assertTrue("Header should contain Assessment ID", header.contains("Assessment ID"));
        assertTrue("Header should contain Assessment Name", header.contains("Assessment Name"));
        assertTrue("Header should contain Assessment AppId", header.contains("Assessment AppId"));
        assertTrue("Header should contain assessment custom field", header.contains("Business Unit"));

        // Data row includes the assessment id, app id, name, and custom field value
        assertNotNull("Should have data row", dataRow);
        assertTrue("Data row should contain assessment id", dataRow.contains("1"));
        assertTrue("Data row should contain assessment name", dataRow.contains("Test Assessment"));
        assertTrue("Data row should contain app id", dataRow.contains("APP-001"));
        assertTrue("Data row should contain custom field value", dataRow.contains("Finance"));

        reader.close();
    }

    @Test
    public void testCustomFieldDefaultValuesAreNotShown() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);

        // Assessment dropdown (LIST) custom type whose defaultValue is the full
        // option list; an unselected field stores that whole list as its value.
        CustomType asmtList = new CustomType();
        asmtList.setId(20L);
        asmtList.setKey("Environment");
        asmtList.setType(0);
        asmtList.setFieldType(2);
        asmtList.setDefaultValue("Dev,QA,Prod");
        asmtList.setDeleted(false);

        // Vulnerability custom type with a plain string default.
        CustomType vulnField = new CustomType();
        vulnField.setId(21L);
        vulnField.setKey("Remediation Owner");
        vulnField.setType(1);
        vulnField.setFieldType(0);
        vulnField.setDefaultValue("PlaceholderOwner");
        vulnField.setDeleted(false);

        Assessment testAsmt = createTestAssessment();
        // Assessment field still holds the default (unselected dropdown).
        CustomField asmtCf = new CustomField();
        asmtCf.setType(asmtList);
        asmtCf.setValue("Dev,QA,Prod");
        testAsmt.setCustomFields(Arrays.asList(asmtCf));

        // Vulnerability: one field left at its default, one genuinely populated.
        Vulnerability vuln = new Vulnerability();
        vuln.setId(1L);
        vuln.setName("SQL Injection");
        vuln.setOpened(new Date());
        vuln.setTracking("VID-001");
        CustomField vulnCfDefault = new CustomField();
        vulnCfDefault.setType(vulnField);
        vulnCfDefault.setValue("PlaceholderOwner");
        vuln.setCustomFields(Arrays.asList(vulnCfDefault));

        mockAllRequiredQueries(Arrays.asList(testAsmt));
        doReturn(mockTypedQuery(Arrays.asList(asmtList)))
            .when(mockEm).createQuery(contains("type = 0"), eq(CustomType.class));
        doReturn(mockTypedQuery(Arrays.asList(vulnField)))
            .when(mockEm).createQuery(contains("type = 1"), eq(CustomType.class));

        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        doReturn(vulnQuery).when(vulnQuery).setParameter(eq("aid"), any());
        doReturn(Arrays.asList(vuln)).when(vulnQuery).getResultList();
        doReturn(vulnQuery).when(mockEm).createQuery(contains("Vulnerability"), eq(Vulnerability.class));

        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }

        // The dropdown's default option list must not leak into the output.
        assertFalse("Default dropdown option list should not be shown",
            content.contains("Dev,QA,Prod"));
        assertFalse("Default dropdown option list should not be shown (quoted)",
            content.contains("\"Dev,QA,Prod\""));
        // The string field still at its default must not be shown either.
        assertFalse("Default custom field value should not be shown",
            content.contains("PlaceholderOwner"));

        reader.close();
    }

    @Test
    public void testPopulatedCustomFieldValuesAreShown() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);

        // Dropdown custom type with a real selection that differs from the default.
        CustomType asmtList = new CustomType();
        asmtList.setId(20L);
        asmtList.setKey("Environment");
        asmtList.setType(0);
        asmtList.setFieldType(2);
        asmtList.setDefaultValue("Dev,QA,Prod");
        asmtList.setDeleted(false);

        Assessment testAsmt = createTestAssessment();
        CustomField asmtCf = new CustomField();
        asmtCf.setType(asmtList);
        asmtCf.setValue("Prod"); // user selected a single option
        testAsmt.setCustomFields(Arrays.asList(asmtCf));

        mockAllRequiredQueries(Arrays.asList(testAsmt));
        doReturn(mockTypedQuery(Arrays.asList(asmtList)))
            .when(mockEm).createQuery(contains("type = 0"), eq(CustomType.class));
        doReturn(mockTypedQuery(new ArrayList<CustomType>()))
            .when(mockEm).createQuery(contains("type = 1"), eq(CustomType.class));

        List<Vulnerability> vulns = createTestVulnerabilities();
        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        doReturn(vulnQuery).when(vulnQuery).setParameter(eq("aid"), any());
        doReturn(vulns).when(vulnQuery).getResultList();
        doReturn(vulnQuery).when(mockEm).createQuery(contains("Vulnerability"), eq(Vulnerability.class));

        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String header = reader.readLine();
        String dataRow = reader.readLine();

        assertTrue("Selected dropdown value should be shown", dataRow.contains("Prod"));
        assertFalse("Full option list should not be shown", dataRow.contains("Dev,QA,Prod"));

        reader.close();
    }

    @Test
    public void testAssessmentCustomTypeQueryExcludesRichtext() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);

        mockAllRequiredQueries(new ArrayList<Assessment>());

        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);

        // The assessment custom type lookup must filter out richtext fields (fieldType 3),
        // which only exist in Enterprise versions, via the "fieldType < 3" predicate.
        verify(mockEm).createQuery(contains("type = 0 and fieldType < 3"), eq(CustomType.class));
    }

    @Test
    public void testCSVDataRowGeneration() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Create test assessment with vulnerabilities
        Assessment testAsmt = createTestAssessment();
        List<Vulnerability> vulns = createTestVulnerabilities();
        
        mockAllRequiredQueries(Arrays.asList(testAsmt));
        
        // Mock vulnerability query
        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        when(vulnQuery.setParameter(eq("aid"), any())).thenReturn(vulnQuery);
        when(vulnQuery.getResultList()).thenReturn(vulns);
        when(mockEm.createQuery(contains("Vulnerability"), eq(Vulnerability.class)))
            .thenReturn(vulnQuery);
        
        String result = action.exportCSV();
        
        assertEquals("Should return SUCCESS", "success", result);
        
        // Read CSV content
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String header = reader.readLine();
        String dataRow = reader.readLine();
        
        assertNotNull("Should have data row", dataRow);
        assertTrue("Data row should contain vulnerability name", dataRow.contains("SQL Injection"));
        assertTrue("Data row should contain assessment name", dataRow.contains("Test Assessment"));
        assertTrue("Data row should contain APP-001", dataRow.contains("APP-001"));
        assertTrue("Data row should contain Critical", dataRow.contains("Critical"));
        assertTrue("Data row should contain tracking ID", dataRow.contains("VID-"));
        
        reader.close();
    }

    @Test
    public void testVulnerabilityDateFiltering() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Set date range
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse("2024-01-01");
        Date endDate = sdf.parse("2024-12-31");
        
        setField(action, "startDate", startDate);
        setField(action, "endDate", endDate);
        
        // Create vulnerabilities with different opened dates
        List<Vulnerability> vulns = new ArrayList<>();
        
        // Vulnerability 1: Opened in date range (should be included)
        Vulnerability vuln1 = new Vulnerability();
        vuln1.setId(1L);
        vuln1.setName("SQL Injection");
        vuln1.setOpened(sdf.parse("2024-06-15"));
        vuln1.setOverall(4L);
        vuln1.setTracking("VID-001");
        vulns.add(vuln1);
        
        // Vulnerability 2: Opened before date range (should be excluded)
        Vulnerability vuln2 = new Vulnerability();
        vuln2.setId(2L);
        vuln2.setName("XSS");
        vuln2.setOpened(sdf.parse("2023-12-15"));
        vuln2.setOverall(3L);
        vuln2.setTracking("VID-002");
        vulns.add(vuln2);
        
        // Vulnerability 3: Opened after date range (should be excluded)
        Vulnerability vuln3 = new Vulnerability();
        vuln3.setId(3L);
        vuln3.setName("CSRF");
        vuln3.setOpened(sdf.parse("2025-01-15"));
        vuln3.setOverall(2L);
        vuln3.setTracking("VID-003");
        vulns.add(vuln3);
        
        Assessment testAsmt = createTestAssessment();
        
        mockAllRequiredQueries(Arrays.asList(testAsmt));
        
        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        when(vulnQuery.setParameter(eq("aid"), any())).thenReturn(vulnQuery);
        when(vulnQuery.getResultList()).thenReturn(vulns);
        when(mockEm.createQuery(contains("Vulnerability"), eq(Vulnerability.class)))
            .thenReturn(vulnQuery);
        
        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }
        
        // Only vuln1 should be in the export
        assertTrue("Should contain SQL Injection (in date range)", content.contains("SQL Injection"));
        assertFalse("Should not contain XSS (before date range)", content.contains("XSS"));
        assertFalse("Should not contain CSRF (after date range)", content.contains("CSRF"));
        
        reader.close();
    }

    @Test
    public void testVulnerabilityWithoutOpenedDateExcluded() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        List<Vulnerability> vulns = new ArrayList<>();
        
        // Vulnerability without opened date
        Vulnerability vuln1 = new Vulnerability();
        vuln1.setId(1L);
        vuln1.setName("No Opened Date");
        vuln1.setOpened(null); // No opened date
        vuln1.setOverall(4L);
        vuln1.setTracking("VID-001");
        vulns.add(vuln1);
        
        // Vulnerability with opened date
        Vulnerability vuln2 = new Vulnerability();
        vuln2.setId(2L);
        vuln2.setName("Has Opened Date");
        vuln2.setOpened(new Date());
        vuln2.setOverall(3L);
        vuln2.setTracking("VID-002");
        vulns.add(vuln2);
        
        Assessment testAsmt = createTestAssessment();
        
        mockAllRequiredQueries(Arrays.asList(testAsmt));
        
        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        when(vulnQuery.setParameter(eq("aid"), any())).thenReturn(vulnQuery);
        when(vulnQuery.getResultList()).thenReturn(vulns);
        when(mockEm.createQuery(contains("Vulnerability"), eq(Vulnerability.class)))
            .thenReturn(vulnQuery);
        
        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }
        
        assertFalse("Should not contain vulnerability without opened date", 
            content.contains("No Opened Date"));
        assertTrue("Should contain vulnerability with opened date", 
            content.contains("Has Opened Date"));
        
        reader.close();
    }

    @Test
    public void testFilenameFormat() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        mockAllRequiredQueries(new ArrayList<Assessment>());
        
        action.exportCSV();
        
        String filename = action.getFilename();
        assertNotNull("Filename should not be null", filename);
        assertTrue("Filename should start with manager_dashboard_vulnerabilities_", 
            filename.startsWith("manager_dashboard_vulnerabilities_"));
        assertTrue("Filename should end with .csv", filename.endsWith(".csv"));
        assertTrue("Filename should contain timestamp", 
            filename.matches("manager_dashboard_vulnerabilities_\\d{8}_\\d{6}\\.csv"));
    }

    @Test
    public void testCSVEscapesSpecialCharacters() throws Exception {
        ManagerDashboardVulnerabilitiesCSV action = new ManagerDashboardVulnerabilitiesCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        Assessment testAsmt = createTestAssessment();
        
        // Vulnerability with special characters
        List<Vulnerability> vulns = new ArrayList<>();
        Vulnerability vuln = new Vulnerability();
        vuln.setId(1L);
        vuln.setName("SQL Injection, \"Severe\" with\nNewline");
        vuln.setOpened(new Date());
        vuln.setOverall(4L);
        vuln.setTracking("VID,001");
        
        Category cat = new Category();
        cat.setName("Injection, \"Critical\"");
        vuln.setCategory(cat);
        
        vulns.add(vuln);
        
        mockAllRequiredQueries(Arrays.asList(testAsmt));
        
        TypedQuery<Vulnerability> vulnQuery = mock(TypedQuery.class);
        when(vulnQuery.setParameter(eq("aid"), any())).thenReturn(vulnQuery);
        when(vulnQuery.getResultList()).thenReturn(vulns);
        when(mockEm.createQuery(contains("Vulnerability"), eq(Vulnerability.class)))
            .thenReturn(vulnQuery);
        
        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);

        // The vulnerability name contains an embedded newline, which is correctly
        // quoted in the CSV but spans two physical lines. Read the whole payload
        // rather than a single line so the escaped fields are all visible.
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            contentBuilder.append(line).append("\n");
        }
        String content = contentBuilder.toString();

        // Verify CSV escaping
        assertTrue("Should escape commas", content.contains("\"VID,001\""));
        assertTrue("Should escape quotes in name", content.contains("\"\"Severe\"\""));
        assertTrue("Should escape category with comma", content.contains("\"Injection, \"\"Critical\"\"\""));

        reader.close();
    }

    // --- Helper Methods ---

    private Assessment createTestAssessment() {
        Assessment asmt = new Assessment();
        asmt.setId(1L);
        asmt.setName("Test Assessment");
        asmt.setAppId("APP-001");
        asmt.setStart(new Date());
        asmt.setEnd(new Date());
        asmt.setStatus("In Progress");
        
        AssessmentType type = new AssessmentType();
        type.setId(1L);
        type.setType("Web Application");
        asmt.setType(type);
        
        asmt.setAssessor(Arrays.asList(managerUser));
        asmt.setCustomFields(new ArrayList<>());
        
        return asmt;
    }

    private List<Vulnerability> createTestVulnerabilities() {
        List<Vulnerability> vulns = new ArrayList<>();
        
        Vulnerability vuln1 = new Vulnerability();
        vuln1.setId(1L);
        vuln1.setName("SQL Injection");
        vuln1.setOpened(new Date());
        vuln1.setOverall(4L); // Critical
        vuln1.setCvssScore("9.8");
        vuln1.setTracking("VID-001");
        
        Category cat1 = new Category();
        cat1.setName("Injection");
        vuln1.setCategory(cat1);
        vuln1.setCustomFields(new ArrayList<>());
        
        vulns.add(vuln1);
        
        Vulnerability vuln2 = new Vulnerability();
        vuln2.setId(2L);
        vuln2.setName("XSS");
        vuln2.setOpened(new Date());
        vuln2.setClosed(new Date());
        vuln2.setOverall(3L); // High
        vuln2.setCvssScore("7.5");
        vuln2.setTracking("VID-002");
        
        Category cat2 = new Category();
        cat2.setName("XSS");
        vuln2.setCategory(cat2);
        vuln2.setCustomFields(new ArrayList<>());
        
        vulns.add(vuln2);
        
        return vulns;
    }

    private List<RiskLevel> createTestRiskLevels() {
        // Risk levels must cover riskId 1-5: Vulnerability.setCvssScore() maps the
        // CVSS score onto overall (>=9.0 -> 5, >=7.0 -> 4, >=4.0 -> 3, >0 -> 2, else 1),
        // so a 9.8 CVSS vulnerability resolves to riskId 5.
        List<RiskLevel> levels = new ArrayList<>();

        RiskLevel critical = new RiskLevel();
        critical.setRiskId(5);
        critical.setRisk("Critical");
        levels.add(critical);

        RiskLevel high = new RiskLevel();
        high.setRiskId(4);
        high.setRisk("High");
        levels.add(high);

        RiskLevel medium = new RiskLevel();
        medium.setRiskId(3);
        medium.setRisk("Medium");
        levels.add(medium);

        RiskLevel low = new RiskLevel();
        low.setRiskId(2);
        low.setRisk("Low");
        levels.add(low);

        RiskLevel info = new RiskLevel();
        info.setRiskId(1);
        info.setRisk("Informational");
        levels.add(info);

        return levels;
    }

    private void mockAllRequiredQueries(List<Assessment> assessments) {
        mockAllRequiredQueriesWithCustomTypes(assessments, new ArrayList<CustomType>());
    }

    private void mockAllRequiredQueriesWithCustomTypes(List<Assessment> assessments, 
                                                       List<CustomType> customTypes) {
        doReturn(mockQuery(new ArrayList<AssessmentType>()))
            .when(mockEm).createQuery("from AssessmentType order by type");
        doReturn(mockQuery(new ArrayList<Teams>()))
            .when(mockEm).createQuery("from Teams order by TeamName");
        doReturn(mockQuery(createTestRiskLevels()))
            .when(mockEm).createQuery("from RiskLevel order by riskId desc");
        doReturn(mockQuery(new ArrayList<Status>()))
            .when(mockEm).createQuery("from Status order by name");
        doReturn(mockTypedQuery(Arrays.asList(managerUser)))
            .when(mockEm).createQuery("from User order by lname, fname", User.class);
        doReturn(mockQuery(new ArrayList<Campaign>()))
            .when(mockEm).createQuery("from Campaign order by name");
        doReturn(mockTypedQuery(customTypes))
            .when(mockEm).createQuery(contains("CustomType"), eq(CustomType.class));
        doReturn(mockQuery(assessments))
            .when(mockEm).createNativeQuery(anyString(), eq(Assessment.class));
    }

    @SuppressWarnings("unchecked")
    private <T> TypedQuery<T> mockTypedQuery(List<T> results) {
        TypedQuery<T> query = mock(TypedQuery.class);
        doReturn(results).when(query).getResultList();
        doReturn(query).when(query).setParameter(anyString(), any());
        return query;
    }

    @SuppressWarnings("unchecked")
    private javax.persistence.Query mockQuery(List<?> results) {
        javax.persistence.Query query = mock(javax.persistence.Query.class);
        doReturn(results).when(query).getResultList();
        doReturn(query).when(query).setParameter(anyString(), any());
        return query;
    }
}
