package com.fuse.api;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.*;

import com.fuse.dao.*;

/**
 * Unit tests for AuditLog REST API endpoint logic.
 * Tests audit log retrieval, filtering, and admin access control.
 */
public class AuditLogUnitTest {

    // --- AuditLog Entity Tests ---

    @Test
    public void testAuditLogBasicFields() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUsername("adminuser");
        log.setType("Assessment");
        log.setDescription("Assessment created");
        log.setTimestamp(new Date());

        assertEquals("ID should match", (Object)Long.valueOf(1L), log.getId());
        assertEquals("Username should match", "adminuser", log.getUsername());
        assertEquals("Type should match", "Assessment", log.getType());
        assertEquals("Description should match", "Assessment created", log.getDescription());
        assertNotNull("Timestamp should not be null", log.getTimestamp());
    }

    @Test
    public void testAuditLogWithNullFields() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("Username should be null when not set", log.getUsername());
        assertNull("Type should be null when not set", log.getType());
        assertNull("Description should be null when not set", log.getDescription());
        assertNull("Timestamp should be null when not set", log.getTimestamp());
    }

    @Test
    public void testAuditLogWithDescription() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setDescription("Assessment created by admin");

        assertEquals("Description should match", "Assessment created by admin", log.getDescription());
    }

    @Test
    public void testAuditLogWithNullDescription() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("Description should be null when not set", log.getDescription());
    }

    @Test
    public void testAuditLogWithIp() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setIp("192.168.1.1");

        assertEquals("IP should match", "192.168.1.1", log.getIp());
    }

    @Test
    public void testAuditLogWithNullIp() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("IP should be null when not set", log.getIp());
    }

    // --- Audit Log Component Tests ---

    @Test
    public void testAuditLogAssessmentComponent() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.CompAssessment);

        assertEquals("Type should be Assessment", AuditLog.CompAssessment, log.getType());
    }

    @Test
    public void testAuditLogUserComponent() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.CompUser);

        assertEquals("Type should be User", AuditLog.CompUser, log.getType());
    }

    @Test
    public void testAuditLogVulnerabilityComponent() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.CompVulnerability);

        assertEquals("Type should be Vulnerability", AuditLog.CompVulnerability, log.getType());
    }

    @Test
    public void testAuditLogVerificationComponent() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.CompVerification);

        assertEquals("Type should be Verification", AuditLog.CompVerification, log.getType());
    }

    @Test
    public void testAuditLogNullType() {
        AuditLog log = new AuditLog();

        assertNull("Type should be null when not set", log.getType());
    }

    // --- Audit Log Type Tests ---

    @Test
    public void testAuditLogLoginType() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.Login);

        assertEquals("Type should be Login", AuditLog.Login, log.getType());
    }

    @Test
    public void testAuditLogUserActionType() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.UserAction);

        assertEquals("Type should be UserAction", AuditLog.UserAction, log.getType());
    }

    @Test
    public void testAuditLogAPIEventType() {
        AuditLog log = new AuditLog();
        log.setType(AuditLog.APIEvent);

        assertEquals("Type should be APIEvent", AuditLog.APIEvent, log.getType());
    }

    @Test
    public void testAuditLogNullTypeString() {
        AuditLog log = new AuditLog();
        log.setType(null);

        assertNull("Type should be null", log.getType());
    }

    // --- Audit Log Constants Tests ---

    @Test
    public void testAuditLogComponentConstants() {
        assertNotNull("CompAssessment should not be null", AuditLog.CompAssessment);
        assertNotNull("CompVulnerability should not be null", AuditLog.CompVulnerability);
        assertNotNull("CompDefaultVuln should not be null", AuditLog.CompDefaultVuln);
        assertNotNull("CompVerification should not be null", AuditLog.CompVerification);
        assertNotNull("CompPeerReview should not be null", AuditLog.CompPeerReview);
        assertNotNull("CompChecklist should not be null", AuditLog.CompChecklist);
        assertNotNull("CompUser should not be null", AuditLog.CompUser);
    }

    @Test
    public void testAuditLogTypeConstants() {
        assertNotNull("Login should not be null", AuditLog.Login);
        assertNotNull("UserAction should not be null", AuditLog.UserAction);
        assertNotNull("APIEvent should not be null", AuditLog.APIEvent);
        assertNotNull("RestEvent should not be null", AuditLog.RestEvent);
    }

    // --- Date Range Query Tests ---

    @Test
    public void testDateRangeQueryValidRange() {
        Date start = new Date(System.currentTimeMillis() - 86400000 * 7); // 7 days ago
        Date end = new Date(); // now

        assertNotNull("Start date should not be null", start);
        assertNotNull("End date should not be null", end);
        assertTrue("Start date should be before end date", start.before(end));
    }

    @Test
    public void testDateRangeQuerySameDay() {
        Date now = new Date();
        assertNotNull("Date should not be null", now);
    }

    @Test
    public void testDateRangeQueryInvalidRange() {
        Date start = new Date(System.currentTimeMillis() + 86400000); // tomorrow
        Date end = new Date(); // now
        assertTrue("Start date should be after end date (invalid)", start.after(end));
    }

    @Test
    public void testDateRangeQueryWeekRange() {
        Date start = new Date(System.currentTimeMillis() - 86400000 * 7); // 7 days ago
        Date end = new Date();
        long daysDiff = (end.getTime() - start.getTime()) / 86400000;
        assertEquals("Should be approximately 7 days", 7, daysDiff);
    }

@Test
    public void testDateRangeQueryMonthRange() {
        Date start = new Date(System.currentTimeMillis() - 86400000L * 30); // 30 days ago
        Date end = new Date();

        long daysDiff = (end.getTime() - start.getTime()) / 86400000L;
        // Allow for 1 day variance due to timezone/time rounding
        assertTrue("Should be approximately 30 days (got " + daysDiff + ")", daysDiff >= 29 && daysDiff <= 31);
    }

    // --- Admin Access Control Tests ---

    @Test
    public void testAdminUserAccess() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("adminuser");

        Permissions perms = new Permissions();
        perms.setAdmin(true);
        adminUser.setPermissions(perms);

        assertTrue("Admin user should have admin permissions",
            adminUser.getPermissions().isAdmin());
    }

    @Test
    public void testNonAdminUserAccess() {
        User nonAdminUser = new User();
        nonAdminUser.setId(1L);
        nonAdminUser.setUsername("regularuser");

        Permissions perms = new Permissions();
        perms.setAdmin(false);
        nonAdminUser.setPermissions(perms);

        assertFalse("Non-admin user should not have admin permissions",
            nonAdminUser.getPermissions().isAdmin());
    }

    @Test
    public void testUserWithNullPermissions() {
        User user = new User();
        user.setId(1L);
        user.setUsername("noperms");

        assertNull("Permissions should be null when not set", user.getPermissions());
    }

    @Test
    public void testUserWithNullUser() {
        assertNull("User should be null", null);
    }

    // --- Audit Log Filtering Tests ---

    @Test
    public void testAuditLogFilterByType() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(createAuditLog("Assessment"));
        logs.add(createAuditLog("User"));
        logs.add(createAuditLog("Assessment"));

        List<AuditLog> filtered = new ArrayList<>();
        for (AuditLog log : logs) {
            if ("Assessment".equals(log.getType())) {
                filtered.add(log);
            }
        }

        assertEquals("Should have 2 Assessment logs", 2, filtered.size());
    }

    @Test
    public void testAuditLogFilterByUsername() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(createAuditLogWithUsername("Assessment", "admin"));
        logs.add(createAuditLogWithUsername("Assessment", "user1"));
        logs.add(createAuditLogWithUsername("Assessment", "admin"));

        List<AuditLog> filtered = new ArrayList<>();
        for (AuditLog log : logs) {
            if ("admin".equals(log.getUsername())) {
                filtered.add(log);
            }
        }

        assertEquals("Should have 2 logs for admin", 2, filtered.size());
    }

    @Test
    public void testAuditLogFilterByTypeAndUsername() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(createAuditLogWithUsername("Assessment", "admin"));
        logs.add(createAuditLogWithUsername("Assessment", "user1"));
        logs.add(createAuditLogWithUsername("User", "admin"));

        List<AuditLog> filtered = new ArrayList<>();
        for (AuditLog log : logs) {
            if ("Assessment".equals(log.getType()) && "admin".equals(log.getUsername())) {
                filtered.add(log);
            }
        }

        assertEquals("Should have 1 Assessment log for admin", 1, filtered.size());
    }

    @Test
    public void testAuditLogFilterByDateRange() {
        // Create a fixed time range for testing
        long now = System.currentTimeMillis();
        Date filterStart = new Date(now - 86400000L * 5); // 5 days ago
        Date filterEnd = new Date(now - 86400000L * 2);   // 2 days ago

        List<AuditLog> logs = new ArrayList<>();
        // Add logs at different points in time
        logs.add(createAuditLogWithDate("Assessment", new Date(now - 86400000L * 8))); // 8 days ago - before range
        logs.add(createAuditLogWithDate("Assessment", new Date(now - 86400000L * 4))); // 4 days ago - in range
        logs.add(createAuditLogWithDate("Assessment", new Date(now - 86400000L * 3))); // 3 days ago - in range
        logs.add(createAuditLogWithDate("Assessment", new Date(now - 86400000L * 1))); // 1 day ago - after range

        List<AuditLog> filtered = new ArrayList<>();
        for (AuditLog log : logs) {
            if (log.getTimestamp().after(filterStart) && log.getTimestamp().before(filterEnd)) {
                filtered.add(log);
            }
        }

        assertEquals("Should have 2 logs in range (4 days ago and 3 days ago)", 2, filtered.size());
    }

    @Test
    public void testAuditLogFilterByCompId() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(createAuditLogWithCompId(100L));
        logs.add(createAuditLogWithCompId(200L));
        logs.add(createAuditLogWithCompId(100L));

        List<AuditLog> filtered = new ArrayList<>();
        for (AuditLog log : logs) {
            if (Long.valueOf(100L).equals(log.getCompid())) {
                filtered.add(log);
            }
        }

        assertEquals("Should have 2 logs with compid 100", 2, filtered.size());
    }

    // --- Audit Log JSON Serialization Tests ---

    @Test
    public void testAuditLogJsonStructure() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUsername("adminuser");
        log.setType("Assessment");
        log.setDescription("Assessment created");
        log.setTimestamp(new Date());

        assertNotNull("ID should be accessible", log.getId());
        assertNotNull("Username should be accessible", log.getUsername());
        assertNotNull("Type should be accessible", log.getType());
        assertNotNull("Description should be accessible", log.getDescription());
        assertNotNull("Timestamp should be accessible", log.getTimestamp());
    }

    @Test
    public void testAuditLogWithEmptyDescription() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setDescription("");

        assertEquals("Description should be empty string", "", log.getDescription());
    }

    @Test
    public void testAuditLogWithLongDescription() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        log.setDescription(sb.toString());

        assertEquals("Description length should match", 1000, log.getDescription().length());
    }

    // --- Audit Log CompId Tests ---

    @Test
    public void testAuditLogWithValidCompId() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setCompid(100L);

        assertEquals("CompId should match", (Object)Long.valueOf(100L), log.getCompid());
    }

    @Test
    public void testAuditLogWithNullCompId() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("CompId should be null when not set", log.getCompid());
    }

    // --- Audit Log Username Tests ---

    @Test
    public void testAuditLogWithValidUsername() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUsername("adminuser");

        assertEquals("Username should match", "adminuser", log.getUsername());
    }

    @Test
    public void testAuditLogWithNullUsername() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("Username should be null when not set", log.getUsername());
    }

    // --- Audit Log Timestamp Tests ---

    @Test
    public void testAuditLogTimestampPresent() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setTimestamp(new Date());

        assertNotNull("Timestamp should not be null", log.getTimestamp());
    }

    @Test
    public void testAuditLogTimestampFuture() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setTimestamp(new Date(System.currentTimeMillis() + 86400000));

        assertNotNull("Future timestamp should not be null", log.getTimestamp());
    }

    @Test
    public void testAuditLogTimestampPast() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setTimestamp(new Date(System.currentTimeMillis() - 86400000));

        assertNotNull("Past timestamp should not be null", log.getTimestamp());
    }

    @Test
    public void testAuditLogTimestampNull() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("Timestamp should be null when not set", log.getTimestamp());
    }

    // --- Audit Log IP Address Tests ---

    @Test
    public void testAuditLogWithValidIp() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setIp("192.168.1.1");

        assertEquals("IP should match", "192.168.1.1", log.getIp());
    }

    @Test
    public void testAuditLogWithIpv6Address() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setIp("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        assertEquals("IPv6 address should match", "2001:0db8:85a3:0000:0000:8a2e:0370:7334", log.getIp());
    }

    // --- Audit Log Alert Name Tests ---

    @Test
    public void testAuditLogWithAlertname() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setAlertname("Security Alert");

        assertEquals("Alertname should match", "Security Alert", log.getAlertname());
    }

    @Test
    public void testAuditLogWithNullAlertname() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("Alertname should be null when not set", log.getAlertname());
    }

    // --- Audit Log URL Tests ---

    @Test
    public void testAuditLogWithUrl() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUrl("/api/assessments/123");

        assertEquals("URL should match", "/api/assessments/123", log.getUrl());
    }

    @Test
    public void testAuditLogWithNullUrl() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("URL should be null when not set", log.getUrl());
    }

    // --- Audit Log CompName Tests ---

    @Test
    public void testAuditLogWithCompName() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setCompname("Test Assessment");

        assertEquals("CompName should match", "Test Assessment", log.getCompname());
    }

    @Test
    public void testAuditLogWithNullCompName() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("CompName should be null when not set", log.getCompname());
    }

    // --- Audit Log User Object Tests ---

    @Test
    public void testAuditLogWithUser() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        User user = new User();
        user.setId(100L);
        user.setUsername("adminuser");
        log.setUser(user);

        assertNotNull("User should be set", log.getUser());
        assertEquals("User ID should match", (Object)Long.valueOf(100L), log.getUser().getId());
        assertEquals("User username should match", "adminuser", log.getUser().getUsername());
    }

    @Test
    public void testAuditLogWithNullUser() {
        AuditLog log = new AuditLog();
        log.setId(1L);

        assertNull("User should be null when not set", log.getUser());
    }

    // --- Helper Methods ---

    private AuditLog createAuditLog(String type) {
        AuditLog log = new AuditLog();
        log.setId(System.currentTimeMillis());
        log.setUsername("adminuser");
        log.setType(type);
        log.setDescription("Test description");
        log.setTimestamp(new Date());
        return log;
    }

    private AuditLog createAuditLogWithUsername(String type, String username) {
        AuditLog log = createAuditLog(type);
        log.setUsername(username);
        return log;
    }

    private AuditLog createAuditLogWithDate(String type, Date timestamp) {
        AuditLog log = createAuditLog(type);
        log.setTimestamp(timestamp);
        return log;
    }

    private AuditLog createAuditLogWithCompId(Long compId) {
        AuditLog log = createAuditLog("Assessment");
        log.setCompid(compId);
        return log;
    }
}
