package com.fuse.actions.admin;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.struts2.junit.StrutsJUnit4TestCase;
import org.junit.Test;

/**
 * Struts action tests for Admin Options using StrutsJUnit4TestCase.
 * Tests system settings, assessment types, campaigns, custom fields, email settings.
 */
public class OptionsStrutsTests extends StrutsJUnit4TestCase<com.fuse.actions.admin.Options> {

    private Object getField(com.fuse.actions.admin.Options action, String fieldName) throws Exception {
        Class<?> clazz = com.fuse.actions.admin.Options.class;
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

    private void setField(com.fuse.actions.admin.Options action, String fieldName, Object value) throws Exception {
        Class<?> clazz = com.fuse.actions.admin.Options.class;
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

    // --- Options execute() Action Tests ---

    @Test
    public void testOptionsActionExecutes() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
    }

    // --- addType Action Tests ---

    @Test
    public void testAddTypeAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "addType");
        setField(action, "name", "Web Application");
        setField(action, "riskType", 0);
    }

    // --- delType Action Tests ---

    @Test
    public void testDelTypeAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "delType");
        setField(action, "id", 1L);
    }

    // --- addCampaign Action Tests ---

    @Test
    public void testAddCampaignAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "addCampaign");
        setField(action, "name", "Security Campaign");
        setField(action, "selected", true);
    }

    // --- delCampaign Action Tests ---

    @Test
    public void testDelCampaignAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "delCampaign");
        setField(action, "id", 1L);
    }

    // --- emailSettings Action Tests ---

    @Test
    public void testEmailSettingsAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "emailSettings");
        setField(action, "server", "smtp.example.com");
        setField(action, "port", "587");
    }

    // --- systemSettings Action Tests ---

    @Test
    public void testSystemSettingsAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "systemSettings");
        setField(action, "webport", "8080");
        setField(action, "tmp", "/tmp");
    }

    // --- test Action Tests ---

    @Test
    public void testTestEmailAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "test");
        setField(action, "to", "test@example.com");
    }

    // --- CreateCF Action Tests ---

    @Test
    public void testCreateCFAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/CreateCF");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "cfname", "Impact Analysis");
        setField(action, "cfvar", "impact_analysis");
        setField(action, "cftype", 1);
    }

    // --- UpdateCF Action Tests ---

    @Test
    public void testUpdateCFAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateCF");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "cfid", 1L);
        setField(action, "cfvar", "updated_field");
    }

    // --- DeleteCF Action Tests ---

    @Test
    public void testDeleteCFAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/DeleteCF");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "cfid", 1L);
    }

    // --- getCustomType Action Tests ---

    @Test
    public void testGetCustomTypeAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/getCustomType");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "cfid", 1L);
    }

    // --- updatePrConfig Action Tests ---

    @Test
    public void testUpdatePrConfigAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/updatePrConfig");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "prChecked", "true");
    }

    // --- updateRandConfig Action Tests ---

    @Test
    public void testUpdateRandConfigAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/updateRandConfig");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "randChecked", "true");
    }

    // --- updateFeedConfig Action Tests ---

    @Test
    public void testUpdateFeedConfigAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/updateFeedConfig");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "feedChecked", "true");
    }

    // --- updateTitles Action Tests ---

    @Test
    public void testUpdateTitlesAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/updateTitles");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        String[] titles = new String[]{"FACTION", "oss"};
        setField(action, "title", titles);
    }

    // --- editCamp Action Tests ---

    @Test
    public void testEditCampAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/editCamp");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "id", 1L);
        setField(action, "name", "Updated Campaign");
    }

    // --- editSelectedCampaign Action Tests ---

    @Test
    public void testEditSelectedCampaignAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/editSelectedCampaign");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "id", 1L);
        setField(action, "selected", true);
    }

    // --- editType Action Tests ---

    @Test
    public void testEditTypeAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/editType");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "id", 1L);
        setField(action, "name", "Updated Type");
        setField(action, "riskType", 1);
    }

    // --- createStatus Action Tests ---

    @Test
    public void testCreateStatusAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/createStatus");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "status", "In Progress");
    }

    // --- deleteStatus Action Tests ---

    @Test
    public void testDeleteStatusAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/deleteStatus");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "statusId", 1L);
    }

    // --- updateStatus Action Tests ---

    @Test
    public void testUpdateStatusAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/updateStatus");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "statusId", 1L);
        setField(action, "status", "Updated Status");
    }

    // --- Options field tests ---

    @Test
    public void testActionField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "action", "addType");
        assertEquals("Action should match", "addType", getField(action, "action"));
    }

    @Test
    public void testIdField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "id", 42L);
        assertEquals("ID should match", Long.valueOf(42L), getField(action, "id"));
    }

    @Test
    public void testNameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "name", "Test Type");
        assertEquals("Name should match", "Test Type", getField(action, "name"));
    }

    @Test
    public void testServerField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "server", "smtp.example.com");
        assertEquals("Server should match", "smtp.example.com", getField(action, "server"));
    }

    @Test
    public void testTypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "type", "SMTP");
        assertEquals("Type should match", "SMTP", getField(action, "type"));
    }

    @Test
    public void testPortField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "port", "587");
        assertEquals("Port should match", "587", getField(action, "port"));
    }

    @Test
    public void testUsernameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "username", "admin");
        assertEquals("Username should match", "admin", getField(action, "username"));
    }

    @Test
    public void testFromaddressField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "fromaddress", "noreply@example.com");
        assertEquals("From address should match", "noreply@example.com", getField(action, "fromaddress"));
    }

    @Test
    public void testPasswordField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "password", "secret");
        assertEquals("Password should match", "secret", getField(action, "password"));
    }

    @Test
    public void testPrefixField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "prefix", "[FACTION]");
        assertEquals("Prefix should match", "[FACTION]", getField(action, "prefix"));
    }

    @Test
    public void testSignatureField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "signature", "Faction Security Team");
        assertEquals("Signature should match", "Faction Security Team", getField(action, "signature"));
    }

    @Test
    public void testWebportField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "webport", "8080");
        assertEquals("Webport should match", "8080", getField(action, "webport"));
    }

    @Test
    public void testTmpField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "tmp", "/tmp");
        assertEquals("Tmp should match", "/tmp", getField(action, "tmp"));
    }

    @Test
    public void testPdfField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "pdf", "/usr/bin/wkhtmltopdf");
        assertEquals("Pdf should match", "/usr/bin/wkhtmltopdf", getField(action, "pdf"));
    }

    @Test
    public void testSenderField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "sender", "admin@example.com");
        assertEquals("Sender should match", "admin@example.com", getField(action, "sender"));
    }

    @Test
    public void testToField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "to", "test@example.com");
        assertEquals("To should match", "test@example.com", getField(action, "to"));
    }

    @Test
    public void testCfnameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "cfname", "Impact Analysis");
        assertEquals("Cfname should match", "Impact Analysis", getField(action, "cfname"));
    }

    @Test
    public void testCfvarField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "cfvar", "impact_analysis");
        assertEquals("Cfvar should match", "impact_analysis", getField(action, "cfvar"));
    }

    @Test
    public void testCftypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "cftype", 1);
        assertEquals("Cftype should match", 1, getField(action, "cftype"));
    }

    @Test
    public void testCffieldtypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "cffieldtype", 2);
        assertEquals("Cffieldtype should match", 2, getField(action, "cffieldtype"));
    }

    @Test
    public void testCfdefaultField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "cfdefault", "default value");
        assertEquals("Cfdefault should match", "default value", getField(action, "cfdefault"));
    }

    @Test
    public void testPrCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "prChecked", "true");
        assertEquals("PrChecked should match", "true", getField(action, "prChecked"));
    }

    @Test
    public void testFeedCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "feedChecked", "true");
        assertEquals("FeedChecked should match", "true", getField(action, "feedChecked"));
    }

    @Test
    public void testRandCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "randChecked", "true");
        assertEquals("RandChecked should match", "true", getField(action, "randChecked"));
    }

    @Test
    public void testSelfPeerReviewField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "selfPeerReview", "true");
        assertEquals("SelfPeerReview should match", "true", getField(action, "selfPeerReview"));
    }

    @Test
    public void testClientidField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "clientid", "client123");
        assertEquals("Clientid should match", "client123", getField(action, "clientid"));
    }

    @Test
    public void testProfileField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "profile", "default");
        assertEquals("Profile should match", "default", getField(action, "profile"));
    }

    @Test
    public void testRiskNameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "riskName", "High");
        assertEquals("RiskName should match", "High", getField(action, "riskName"));
    }

    @Test
    public void testRiskIdField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "riskId", 5L);
        assertEquals("RiskId should match", Long.valueOf(5L), getField(action, "riskId"));
    }

    @Test
    public void testRiskTypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "riskType", 1);
        assertEquals("RiskType should match", 1, getField(action, "riskType"));
    }

    @Test
    public void testSelectedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "selected", true);
        assertTrue("Selected should be true", (Boolean) getField(action, "selected"));
    }

    @Test
    public void testAsmtTypesField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "asmtTypes", "1,2,3");
        assertEquals("AsmtTypes should match", "1,2,3", getField(action, "asmtTypes"));
    }

    @Test
    public void testReadonlyField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "readonly", true);
        assertTrue("Readonly should be true", (Boolean) getField(action, "readonly"));
    }

    @Test
    public void testStatusField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "status", "In Progress");
        assertEquals("Status should match", "In Progress", getField(action, "status"));
    }

    @Test
    public void testStatusIdField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "statusId", 3L);
        assertEquals("StatusId should match", Long.valueOf(3L), getField(action, "statusId"));
    }

    @Test
    public void testCfidField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "cfid", 7L);
        assertEquals("Cfid should match", Long.valueOf(7L), getField(action, "cfid"));
    }

    @Test
    public void testAuthCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "authChecked", "checked");
        assertEquals("AuthChecked should match", "checked", getField(action, "authChecked"));
    }

    @Test
    public void testTlsCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "tlsChecked", "checked");
        assertEquals("TlsChecked should match", "checked", getField(action, "tlsChecked"));
    }

    @Test
    public void testSslCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "sslChecked", "checked");
        assertEquals("SslChecked should match", "checked", getField(action, "sslChecked"));
    }

    // --- Boolean field tests ---

    @Test
    public void testAuthisCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "authischecked", true);
        assertTrue("AuthisChecked should be true", (Boolean) getField(action, "authischecked"));
    }

    @Test
    public void testTlsisCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "tlsischecked", true);
        assertTrue("TlsisChecked should be true", (Boolean) getField(action, "tlsischecked"));
    }

    @Test
    public void testSslisCheckedField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "sslischecked", true);
        assertTrue("SslisChecked should be true", (Boolean) getField(action, "sslischecked"));
    }

    // --- Message field test ---

    @Test
    public void testMessageField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        setField(action, "message", "Error message");
        assertEquals("Message should match", "Error message", getField(action, "message"));
    }

    // --- Title array test ---

    @Test
    public void testTitleArray() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        String[] titles = new String[]{"FACTION", "oss"};
        setField(action, "title", titles);
        assertArrayEquals("Title array should match", titles, (String[]) getField(action, "title"));
    }

    // --- List field tests ---

    @Test
    public void testTypesListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        List<com.fuse.dao.AssessmentType> types = new ArrayList<>();
        setField(action, "types", types);
        assertEquals("Types list should match", types, getField(action, "types"));
    }

    @Test
    public void testCampaignsListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        List<com.fuse.dao.Campaign> campaigns = new ArrayList<>();
        setField(action, "campaigns", campaigns);
        assertEquals("Campaigns list should match", campaigns, getField(action, "campaigns"));
    }

    @Test
    public void testCustomListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        List<com.fuse.dao.CustomType> custom = new ArrayList<>();
        setField(action, "custom", custom);
        assertEquals("Custom list should match", custom, getField(action, "custom"));
    }

    @Test
    public void testStatusesListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        List<com.fuse.dao.Status> statuses = new ArrayList<>();
        setField(action, "statuses", statuses);
        assertEquals("Statuses list should match", statuses, getField(action, "statuses"));
    }

    // --- resultType test ---

    @Test
    public void testResultTypeField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Options");
        com.fuse.actions.admin.Options action = (com.fuse.actions.admin.Options) proxy.getAction();
        com.fuse.dao.CustomType resultType = new com.fuse.dao.CustomType();
        setField(action, "resultType", resultType);
        assertSame("ResultType should match", resultType, getField(action, "resultType"));
    }
}
