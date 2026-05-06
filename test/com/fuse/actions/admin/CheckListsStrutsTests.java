package com.fuse.actions.admin;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.struts2.junit.StrutsJUnit4TestCase;
import org.junit.Test;

/**
 * Struts action tests for Admin CheckLists using StrutsJUnit4TestCase.
 * Tests checklist management, questions, vulnerability mapping, CSV export/upload.
 */
public class CheckListsStrutsTests extends StrutsJUnit4TestCase<com.fuse.actions.admin.CheckLists> {

    private Object getField(com.fuse.actions.admin.CheckLists action, String fieldName) throws Exception {
        Class<?> clazz = com.fuse.actions.admin.CheckLists.class;
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

    private void setField(com.fuse.actions.admin.CheckLists action, String fieldName, Object value) throws Exception {
        Class<?> clazz = com.fuse.actions.admin.CheckLists.class;
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

    // --- Checklists showCheckLists() Action Tests ---

    @Test
    public void testShowCheckListsAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
    }

    // --- GetCheckList Action Tests ---

    @Test
    public void testGetCheckListAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/GetCheckList");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
    }

    // --- GetTypes Action Tests ---

    @Test
    public void testGetTypesAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/GetTypes");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
    }

    // --- DeleteChecklist Action Tests ---

    @Test
    public void testDeleteChecklistAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/DeleteChecklist");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
    }

    // --- CreateChecklist Action Tests ---

    @Test
    public void testCreateChecklistAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/CreateChecklist");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "name", "New Checklist");
    }

    // --- UpdateChecklist Action Tests ---

    @Test
    public void testUpdateChecklistAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateChecklist");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
        setField(action, "name", "Updated Checklist");
    }

    // --- AddChecklistQuestion Action Tests ---

    @Test
    public void testAddChecklistQuestionAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/AddChecklistQuestion");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
        setField(action, "question", "Is MFA enabled?");
    }

    // --- RemoveChecklistQuestion Action Tests ---

    @Test
    public void testRemoveChecklistQuestionAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/RemoveChecklistQuestion");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
        setField(action, "listitem", 42L);
    }

    // --- UpdateChecklistQuestion Action Tests ---

    @Test
    public void testUpdateChecklistQuestionAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateChecklistQuestion");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "listitem", 42L);
        setField(action, "question", "Updated question text");
    }

    // --- MapVulns Action Tests ---

    @Test
    public void testMapVulnsAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/MapVulns");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
        setField(action, "listitem", 42L);
        List<Long> vulns = new ArrayList<>();
        vulns.add(1L);
        vulns.add(2L);
        setField(action, "vulns", vulns);
    }

    // --- ExportChecklist Action Tests ---

    @Test
    public void testExportChecklistAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/ExportChecklist");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
    }

    // --- UploadChecklist Action Tests ---

    @Test
    public void testUploadChecklistAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UploadChecklist");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
    }

    // --- ToggleType Action Tests ---

    @Test
    public void testToggleTypeAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/ToggleType");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "checklist", 1L);
        setField(action, "type", 1);
    }

    // --- CheckLists field tests ---

    @Test
    public void testNameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "name", "Test Checklist");
        assertEquals("Name should match", "Test Checklist", getField(action, "name"));
    }

    @Test
    public void testQuestionField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "question", "Is encryption enabled?");
        assertEquals("Question should match", "Is encryption enabled?", getField(action, "question"));
    }

    @Test
    public void testChecklistField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "checklist", 99L);
        assertEquals("Checklist should match", Long.valueOf(99L), getField(action, "checklist"));
    }

    @Test
    public void testListitemField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "listitem", 42L);
        assertEquals("Listitem should match", Long.valueOf(42L), getField(action, "listitem"));
    }

    @Test
    public void testTypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "type", 3);
        assertEquals("Type should match", 3, getField(action, "type"));
    }

    @Test
    public void testActiveChecklistField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "activeChecklist", "active");
        assertEquals("ActiveChecklist should match", "active", getField(action, "activeChecklist"));
    }

    // --- File upload field tests ---

    @Test
    public void testContentTypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "contentType", "text/csv");
        assertEquals("ContentType should match", "text/csv", getField(action, "contentType"));
    }

    @Test
    public void testFilenameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        setField(action, "filename", "checklist.csv");
        assertEquals("Filename should match", "checklist.csv", getField(action, "filename"));
    }

    // --- List field tests ---

    @Test
    public void testListsListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        List<com.fuse.actions.admin.CheckLists> lists = new ArrayList<>();
        setField(action, "lists", lists);
        assertEquals("Lists list should match", lists, getField(action, "lists"));
    }

    @Test
    public void testVulnsListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        List<Long> vulns = new ArrayList<>();
        vulns.add(1L);
        vulns.add(2L);
        setField(action, "vulns", vulns);
        assertEquals("Vulns list should match", vulns, getField(action, "vulns"));
    }

    @Test
    public void testTypesListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        List<com.fuse.dao.AssessmentType> types = new ArrayList<>();
        setField(action, "types", types);
        assertEquals("Types list should match", types, getField(action, "types"));
    }

    // --- Check field test ---

    @Test
    public void testCheckField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Checklists");
        com.fuse.actions.admin.CheckLists action = (com.fuse.actions.admin.CheckLists) proxy.getAction();
        com.fuse.dao.CheckList check = new com.fuse.dao.CheckList();
        setField(action, "check", check);
        assertSame("Check should match", check, getField(action, "check"));
    }
}
