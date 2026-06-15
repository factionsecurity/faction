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
 * Tests for ManagerDashboardCSV action - Assessment CSV Export
 * Tests authentication, authorization, and CSV content generation
 */
public class ManagerDashboardCSVTest extends StrutsJUnit4TestCase<ManagerDashboardCSV> {

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

    private Object getField(ManagerDashboardCSV action, String fieldName) throws Exception {
        Class<?> clazz = ManagerDashboardCSV.class;
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

    private void setField(ManagerDashboardCSV action, String fieldName, Object value) throws Exception {
        Class<?> clazz = ManagerDashboardCSV.class;
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
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        
        // Don't set manager role in ActionContext (simulates non-manager)
        // The isAcmanager() method will return false
        
        String result = action.exportCSV();
        
        assertEquals("Should return LOGIN for non-manager user", "login", result);
        assertNull("Should not generate CSV for non-manager", action.getInputStream());
    }

    @Test
    public void testActionAllowsManagerAccess() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        
        // Mock the EntityManager to avoid database calls
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        
        // Set manager role in ActionContext
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Mock required data
        doReturn(mockQuery(new ArrayList<AssessmentType>()))
            .when(mockEm).createQuery("from AssessmentType order by type");
        doReturn(mockQuery(new ArrayList<Teams>()))
            .when(mockEm).createQuery("from Teams order by TeamName");
        doReturn(mockQuery(new ArrayList<RiskLevel>()))
            .when(mockEm).createQuery("from RiskLevel order by riskId desc");
        doReturn(mockQuery(new ArrayList<Status>()))
            .when(mockEm).createQuery("from Status order by name");
        doReturn(mockTypedQuery(Arrays.asList(managerUser)))
            .when(mockEm).createQuery("from User order by lname, fname", User.class);
        doReturn(mockQuery(new ArrayList<Campaign>()))
            .when(mockEm).createQuery("from Campaign order by name");
        doReturn(mockTypedQuery(new ArrayList<CustomType>()))
            .when(mockEm).createQuery(contains("CustomType"), eq(CustomType.class));
        doReturn(mockQuery(new ArrayList<Assessment>()))
            .when(mockEm).createNativeQuery(anyString(), eq(Assessment.class));

        String result = action.exportCSV();

        assertEquals("Should return SUCCESS for manager user", "success", result);
        assertNotNull("Should generate CSV for manager", action.getInputStream());
        assertNotNull("Should set filename", action.getFilename());
    }

    @Test
    public void testActionRequiresLogin() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        
        // No manager role set (not logged in or no permissions)
        // ActionContext will not have isManager = true
        
        String result = action.exportCSV();
        
        assertEquals("Should return LOGIN for unauthenticated user", "login", result);
    }

    // --- CSV Content Tests ---

    @Test
    public void testCSVHeaderGeneration() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Set up risk levels
        RiskLevel critical = new RiskLevel();
        critical.setRiskId(4);
        critical.setRisk("Critical");
        
        RiskLevel high = new RiskLevel();
        high.setRiskId(3);
        high.setRisk("High");
        
        List<RiskLevel> riskLevels = Arrays.asList(critical, high);
        
        // Set up custom types
        CustomType customField1 = new CustomType();
        customField1.setId(1L);
        customField1.setKey("Business Unit");
        customField1.setType(0); // ASMT type
        customField1.setFieldType(0);
        customField1.setDeleted(false);
        
        List<CustomType> customTypes = Arrays.asList(customField1);
        
        // Mock queries
        doReturn(mockQuery(new ArrayList<AssessmentType>()))
            .when(mockEm).createQuery("from AssessmentType order by type");
        doReturn(mockQuery(new ArrayList<Teams>()))
            .when(mockEm).createQuery("from Teams order by TeamName");
        doReturn(mockQuery(riskLevels))
            .when(mockEm).createQuery("from RiskLevel order by riskId desc");
        doReturn(mockQuery(new ArrayList<Status>()))
            .when(mockEm).createQuery("from Status order by name");
        doReturn(mockTypedQuery(Arrays.asList(managerUser)))
            .when(mockEm).createQuery("from User order by lname, fname", User.class);
        doReturn(mockQuery(new ArrayList<Campaign>()))
            .when(mockEm).createQuery("from Campaign order by name");
        doReturn(mockTypedQuery(customTypes))
            .when(mockEm).createQuery(contains("CustomType"), eq(CustomType.class));
        doReturn(mockQuery(new ArrayList<Assessment>()))
            .when(mockEm).createNativeQuery(anyString(), eq(Assessment.class));
        
        String result = action.exportCSV();
        
        assertEquals("Should return SUCCESS", "success", result);
        
        // Read CSV content
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String header = reader.readLine();
        
        // Verify header contains all expected columns
        assertTrue("Header should contain AppId", header.contains("AppId"));
        assertTrue("Header should contain Name", header.contains("Name"));
        assertTrue("Header should contain Type", header.contains("Type"));
        assertTrue("Header should contain Team", header.contains("Team"));
        assertTrue("Header should contain Assessor", header.contains("Assessor"));
        assertTrue("Header should contain Status", header.contains("Status"));
        assertTrue("Header should contain custom field", header.contains("Business Unit"));
        assertTrue("Header should contain Critical", header.contains("Critical"));
        assertTrue("Header should contain High", header.contains("High"));
        
        reader.close();
    }

    @Test
    public void testCSVDataRowGeneration() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Create test assessment
        Assessment testAsmt = createTestAssessment();
        
        // Set up mock data
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
        doReturn(mockTypedQuery(new ArrayList<CustomType>()))
            .when(mockEm).createQuery(contains("CustomType"), eq(CustomType.class));
        doReturn(mockQuery(Arrays.asList(testAsmt)))
            .when(mockEm).createNativeQuery(anyString(), eq(Assessment.class));
        doReturn(mockTypedQuery(new ArrayList<Vulnerability>()))
            .when(mockEm).createQuery(contains("Vulnerability"), eq(Vulnerability.class));
        
        String result = action.exportCSV();
        
        assertEquals("Should return SUCCESS", "success", result);
        
        // Read CSV content
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String header = reader.readLine();
        String dataRow = reader.readLine();
        
        assertNotNull("Should have data row", dataRow);
        assertTrue("Data row should contain APP-001", dataRow.contains("APP-001"));
        assertTrue("Data row should contain Test Assessment", dataRow.contains("Test Assessment"));
        assertTrue("Data row should contain Web Application", dataRow.contains("Web Application"));
        
        reader.close();
    }

    @Test
    public void testCSVEscapesSpecialCharacters() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Create assessment with special characters
        Assessment testAsmt = createTestAssessment();
        testAsmt.setName("Test, \"Assessment\" with\nSpecial Characters");
        testAsmt.setAppId("APP,001");
        
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
        doReturn(mockTypedQuery(new ArrayList<CustomType>()))
            .when(mockEm).createQuery(contains("CustomType"), eq(CustomType.class));
        doReturn(mockQuery(Arrays.asList(testAsmt)))
            .when(mockEm).createNativeQuery(anyString(), eq(Assessment.class));
        doReturn(mockTypedQuery(new ArrayList<Vulnerability>()))
            .when(mockEm).createQuery(contains("Vulnerability"), eq(Vulnerability.class));
        
        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        reader.readLine(); // Skip header
        String dataRow = reader.readLine();
        
        // Verify CSV escaping (quotes should be escaped, values with special chars wrapped in quotes)
        assertTrue("Should escape commas", dataRow.contains("\"APP,001\""));
        assertTrue("Should escape quotes", dataRow.contains("\"\"Assessment\"\""));
        
        reader.close();
    }

    @Test
    public void testCustomFieldDefaultValuesAreNotShown() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);

        // Dropdown (LIST) custom type whose defaultValue is the full option list.
        CustomType listType = new CustomType();
        listType.setId(20L);
        listType.setKey("Environment");
        listType.setType(0);
        listType.setFieldType(2);
        listType.setDefaultValue("Dev,QA,Prod");
        listType.setDeleted(false);

        Assessment testAsmt = createTestAssessment();
        // Unselected dropdown: stored value still equals the default option list.
        CustomField unselected = new CustomField();
        unselected.setType(listType);
        unselected.setValue("Dev,QA,Prod");
        testAsmt.setCustomFields(Arrays.asList(unselected));

        mockAllRequiredQueries(Arrays.asList(testAsmt));
        doReturn(mockTypedQuery(Arrays.asList(listType)))
            .when(mockEm).createQuery(contains("type = 0"), eq(CustomType.class));

        String result = action.exportCSV();
        assertEquals("Should return SUCCESS", "success", result);

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(action.getInputStream()));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }

        assertFalse("Default dropdown option list should not be shown",
            content.contains("Dev,QA,Prod"));

        reader.close();
    }

    @Test
    public void testFilenameFormat() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);

        mockAllRequiredQueries(new ArrayList<Assessment>());

        action.exportCSV();

        String filename = action.getFilename();
        assertNotNull("Filename should not be null", filename);
        assertTrue("Filename should start with manager_dashboard_assessments_", 
            filename.startsWith("manager_dashboard_assessments_"));
        assertTrue("Filename should end with .csv", filename.endsWith(".csv"));
        assertTrue("Filename should contain timestamp", 
            filename.matches("manager_dashboard_assessments_\\d{8}_\\d{6}\\.csv"));
    }

    // --- Date Filtering Tests ---

    @Test
    public void testDateRangeFiltering() throws Exception {
        ManagerDashboardCSV action = new ManagerDashboardCSV();
        mockEm = mock(EntityManager.class);
        setField(action, "em", mockEm);
        com.opensymphony.xwork2.ActionContext.getContext().put("isManager", true);
        
        // Set date range
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse("2024-01-01");
        Date endDate = sdf.parse("2024-12-31");
        
        setField(action, "startDate", startDate);
        setField(action, "endDate", endDate);
        
        mockAllRequiredQueries(new ArrayList<Assessment>());
        
        String result = action.exportCSV();
        
        assertEquals("Should return SUCCESS", "success", result);
        
        // Verify the MongoDB query was called with date parameters
        verify(mockEm).createNativeQuery(contains("$and"), eq(Assessment.class));
        verify(mockEm).createNativeQuery(contains("start"), eq(Assessment.class));
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

    private List<RiskLevel> createTestRiskLevels() {
        List<RiskLevel> levels = new ArrayList<>();
        
        RiskLevel critical = new RiskLevel();
        critical.setRiskId(4);
        critical.setRisk("Critical");
        levels.add(critical);
        
        RiskLevel high = new RiskLevel();
        high.setRiskId(3);
        high.setRisk("High");
        levels.add(high);
        
        RiskLevel medium = new RiskLevel();
        medium.setRiskId(2);
        medium.setRisk("Medium");
        levels.add(medium);
        
        RiskLevel low = new RiskLevel();
        low.setRiskId(1);
        low.setRisk("Low");
        levels.add(low);
        
        return levels;
    }

    private void mockAllRequiredQueries(List<Assessment> assessments) {
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
        doReturn(mockTypedQuery(new ArrayList<CustomType>()))
            .when(mockEm).createQuery(contains("CustomType"), eq(CustomType.class));
        doReturn(mockQuery(assessments))
            .when(mockEm).createNativeQuery(anyString(), eq(Assessment.class));
        doReturn(mockTypedQuery(new ArrayList<Vulnerability>()))
            .when(mockEm).createQuery(contains("Vulnerability"), eq(Vulnerability.class));
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
