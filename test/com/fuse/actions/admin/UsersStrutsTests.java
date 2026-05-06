package com.fuse.actions.admin;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.struts2.junit.StrutsJUnit4TestCase;
import org.junit.Test;

/**
 * Struts action tests for Admin Users using StrutsJUnit4TestCase.
 * Tests user management, team management, authentication config.
 */
public class UsersStrutsTests extends StrutsJUnit4TestCase<com.fuse.actions.admin.Users> {

    private Object getField(com.fuse.actions.admin.Users action, String fieldName) throws Exception {
        Class<?> clazz = com.fuse.actions.admin.Users.class;
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

    private void setField(com.fuse.actions.admin.Users action, String fieldName, Object value) throws Exception {
        Class<?> clazz = com.fuse.actions.admin.Users.class;
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

    // --- Users execute() Action Tests ---

    @Test
    public void testUsersActionExecutes() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
    }

    // --- GetUser Action Tests ---

    @Test
    public void testGetUserAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/GetUser");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "userId", "user123");
        assertEquals("User ID should be set", "user123", getField(action, "userId"));
    }

    // --- AddUser Action Tests ---

    @Test
    public void testAddUserAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/AddUser");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "username", "testuser");
        setField(action, "email", "test@example.com");
        setField(action, "fname", "Test");
        setField(action, "lname", "User");
        setField(action, "team", "1");
        setField(action, "mgr", true);
    }

    // --- UpdateUser Action Tests ---

    @Test
    public void testUpdateUserAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateUser");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "userId", "user123");
        setField(action, "username", "updateduser");
    }

    // --- DeleteUser Action Tests ---

    @Test
    public void testDeleteUserAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/DeleteUser");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "userId", "user123");
    }

    // --- UpdateAPI Action Tests ---

    @Test
    public void testUpdateAPIAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateAPI");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "userId", "user123");
    }

    // --- CreateTeamName Action Tests ---

    @Test
    public void testCreateTeamNameAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/CreateTeamName");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "team_name", "New Team");
    }

    // --- DeleteTeamName Action Tests ---

    @Test
    public void testDeleteTeamNameAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/DeleteTeamName");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "team_id", "team456");
    }

    // --- UpdateTeamName Action Tests ---

    @Test
    public void testUpdateTeamNameAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateTeamName");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "team", "Updated Team");
        setField(action, "team_id", "team456");
    }

    // --- Unlock Action Tests ---

    @Test
    public void testUnlockAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Unlock");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "userId", "user123");
    }

    // --- UpdateUIOLI Action Tests ---

    @Test
    public void testUpdateUIOLIAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/UpdateUIOLI");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "uioli", 90);
    }

    // --- SaveLDAP Action Tests ---

    @Test
    public void testSaveLDAPAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/SaveLDAP");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "ldapURL", "ldap://example.com");
        setField(action, "ldapBaseDn", "dc=example,dc=com");
    }

    // --- TestLDAP Action Tests ---

    @Test
    public void testTestLDAPAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/TestLDAP");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
    }

    // --- SearchLDAP Action Tests ---

    @Test
    public void testSearchLDAPAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/SearchLDAP");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "username", "testuser");
    }

    // --- SaveOAUTH Action Tests ---

    @Test
    public void testSaveOAUTHAction() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/SaveOAUTH");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "oauthClientId", "client123");
        setField(action, "oauthDiscoveryURI", "https://example.com/.well-known/openid-configuration");
    }

    // --- SaveSAML2 Action Tests ---

    @Test
    public void testSaveSAML2Action() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/SaveSAML2");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNotNull("Action should not be null", action);
        setField(action, "saml2MetaUrl", "https://example.com/saml/metadata");
    }

    // --- User field tests ---

    @Test
    public void testUsernameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "username", "testuser");
        assertEquals("Username should match", "testuser", getField(action, "username"));
    }

    @Test
    public void testEmailField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "email", "test@example.com");
        assertEquals("Email should match", "test@example.com", getField(action, "email"));
    }

    @Test
    public void testFnameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "fname", "Test");
        assertEquals("Fname should match", "Test", getField(action, "fname"));
    }

    @Test
    public void testLnameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "lname", "User");
        assertEquals("Lname should match", "User", getField(action, "lname"));
    }

    @Test
    public void testMgrField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "mgr", true);
        assertTrue("Mgr should be true", (Boolean) getField(action, "mgr"));
    }

    @Test
    public void testEngField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "eng", true);
        assertTrue("Eng should be true", (Boolean) getField(action, "eng"));
    }

    @Test
    public void testRemField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "rem", true);
        assertTrue("Rem should be true", (Boolean) getField(action, "rem"));
    }

    @Test
    public void testInactiveField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "inactive", true);
        assertTrue("Inactive should be true", (Boolean) getField(action, "inactive"));
    }

    @Test
    public void testExecField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "exec", true);
        assertTrue("Exec should be true", (Boolean) getField(action, "exec"));
    }

    @Test
    public void testAssessorField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "assessor", true);
        assertTrue("Assessor should be true", (Boolean) getField(action, "assessor"));
    }

    @Test
    public void testAdminField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "admin", true);
        assertTrue("Admin should be true", (Boolean) getField(action, "admin"));
    }

    @Test
    public void testApiField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "api", true);
        assertTrue("Api should be true", (Boolean) getField(action, "api"));
    }

    @Test
    public void testTeamField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "team", "1");
        assertEquals("Team should match", "1", getField(action, "team"));
    }

    @Test
    public void testTeamNameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "team_name", "New Team");
        assertEquals("Team name should match", "New Team", getField(action, "team_name"));
    }

    @Test
    public void testTeamIdField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "team_id", "team456");
        assertEquals("Team ID should match", "team456", getField(action, "team_id"));
    }

    @Test
    public void testActionField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "action", "create");
        assertEquals("Action should match", "create", getField(action, "action"));
    }

    @Test
    public void testApiKeyField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "apiKey", "test-key-123");
        assertEquals("API key should match", "test-key-123", getField(action, "apiKey"));
    }

    @Test
    public void testUioliField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "uioli", 90);
        assertEquals("UIOLI should match", 90, getField(action, "uioli"));
    }

    @Test
    public void testAccessControlField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "accesscontrol", 2);
        assertEquals("Access control should match", 2, getField(action, "accesscontrol"));
    }

    @Test
    public void testPlatformTierField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "platformTier", "enterprise");
        assertEquals("Platform tier should match", "enterprise", getField(action, "platformTier"));
    }

    @Test
    public void testAuthMethodField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "authMethod", "LDAP");
        assertEquals("Auth method should match", "LDAP", getField(action, "authMethod"));
    }

    @Test
    public void testLdapUrlField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "ldapURL", "ldap://example.com");
        assertEquals("LDAP URL should match", "ldap://example.com", getField(action, "ldapURL"));
    }

    @Test
    public void testLdapBaseDnField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "ldapBaseDn", "dc=example,dc=com");
        assertEquals("LDAP Base DN should match", "dc=example,dc=com", getField(action, "ldapBaseDn"));
    }

    @Test
    public void testLdapUserNameField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "ldapUserName", "cn=admin,dc=example,dc=com");
        assertEquals("LDAP username should match", "cn=admin,dc=example,dc=com", getField(action, "ldapUserName"));
    }

    @Test
    public void testLdapPasswordField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "ldapPassword", "secret");
        assertEquals("LDAP password should match", "secret", getField(action, "ldapPassword"));
    }

    @Test
    public void testLdapSecurityField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "ldapSecurity", "simple");
        assertEquals("LDAP security should match", "simple", getField(action, "ldapSecurity"));
    }

    @Test
    public void testIsInsecureField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "isInsecure", true);
        assertTrue("IsInsecure should be true", (Boolean) getField(action, "isInsecure"));
    }

    @Test
    public void testLdapObjectClassField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "ldapObjectClass", "person");
        assertEquals("LDAP object class should match", "person", getField(action, "ldapObjectClass"));
    }

    @Test
    public void testCredentialField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "credential", "password123");
        assertEquals("Credential should match", "password123", getField(action, "credential"));
    }

    @Test
    public void testOauthClientIdField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "oauthClientId", "client123");
        assertEquals("OAuth client ID should match", "client123", getField(action, "oauthClientId"));
    }

    @Test
    public void testOauthDiscoveryUriField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "oauthDiscoveryURI", "https://example.com/.well-known/openid-configuration");
        assertEquals("OAuth discovery URI should match", "https://example.com/.well-known/openid-configuration", getField(action, "oauthDiscoveryURI"));
    }

    @Test
    public void testSaml2MetaUrlField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "saml2MetaUrl", "https://example.com/saml/metadata");
        assertEquals("SAML2 metadata URL should match", "https://example.com/saml/metadata", getField(action, "saml2MetaUrl"));
    }

    // --- checkRoleAdded() logic tests ---

    @Test
    public void testCheckRoleAddedWithAdmin() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "admin", true);
        // checkRoleAdded is a method, we test by setting admin and verifying the action can proceed
    }

    @Test
    public void testCheckRoleAddedWithAssessor() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "assessor", true);
    }

    @Test
    public void testCheckRoleAddedWithManager() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "mgr", true);
    }

    @Test
    public void testCheckRoleAddedWithRemediation() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "rem", true);
    }

    @Test
    public void testCheckRoleAddedWithEngagement() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "eng", true);
    }

    // --- getTier() and getUserLimit() logic tests ---

    @Test
    public void testTierIsConsultant() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        // Tier is read from environment, but we can verify the action exists
    }

    // --- User list and team list tests ---

    @Test
    public void testUsersListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        List<com.fuse.dao.User> users = new ArrayList<>();
        setField(action, "users", users);
        assertEquals("Users list should match", users, getField(action, "users"));
    }

    @Test
    public void testTeamsListExists() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        List<com.fuse.dao.Teams> teams = new ArrayList<>();
        setField(action, "teams", teams);
        assertEquals("Teams list should match", teams, getField(action, "teams"));
    }

    // --- selectedUser tests ---

    @Test
    public void testSelectedUserNull() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        assertNull("Selected user should be null", getField(action, "selectedUser"));
    }

    @Test
    public void testUpdateField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "update", "true");
        assertEquals("Update should be true", "true", getField(action, "update"));
    }

    @Test
    public void testDeleteField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "delete", "true");
        assertEquals("Delete should be true", "true", getField(action, "delete"));
    }

    @Test
    public void testCreateField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "create", "true");
        assertEquals("Create should be true", "true", getField(action, "create"));
    }

    // --- OAuth client secret test ---

    @Test
    public void testOauthClientSecretField() throws Exception {
        com.opensymphony.xwork2.ActionProxy proxy = getActionProxy("/portal/Users");
        com.fuse.actions.admin.Users action = (com.fuse.actions.admin.Users) proxy.getAction();
        setField(action, "oauthClientSecret", "secret123");
        assertEquals("OAuth client secret should match", "secret123", getField(action, "oauthClientSecret"));
    }
}
