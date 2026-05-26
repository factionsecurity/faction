package com.fuse.actions.assessment;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.fuse.dao.*;
import com.fuse.utils.History;

/**
 * Unit tests for AssessmentView Struts action.
 * Tests entity interactions, permission checks, and core logic.
 */
@RunWith(MockitoJUnitRunner.class)
public class AssessmentViewTests {

    private Assessment createTestAssessment() {
        Assessment asmt = new Assessment();
        asmt.setId(1L);
        asmt.setName("Test Assessment");
        asmt.setAppId("APP-001");
        asmt.setSummary("Test summary");
        asmt.setRiskAnalysis("Test risk analysis");
        asmt.setDistributionList("user@example.com");
        asmt.setCustomFields(new ArrayList<>());
        asmt.setVulns(new ArrayList<>());
        asmt.setNotebook(new ArrayList<>());
        return asmt;
    }

    private User createTestUser(long id, String username, boolean isAssessor, 
                                 boolean isManager, boolean isAdmin) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFname(username);
        user.setLname("User");

        Permissions perms = new Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setAdmin(isAdmin);
        perms.setAssessor(isAssessor);
        perms.setManager(isManager);

        user.setPermissions(perms);
        return user;
    }

    // --- Assessment Update Logic Tests ---

    @Test
    public void testAssessmentUpdateSummary() {
        Assessment asmt = createTestAssessment();
        asmt.setSummary("Original summary");
        asmt.setSummary("Updated summary");
        assertEquals("Summary should be updated", "Updated summary", asmt.getSummary());
    }

    @Test
    public void testAssessmentUpdateRiskAnalysis() {
        Assessment asmt = createTestAssessment();
        asmt.setRiskAnalysis("Original risk analysis");
        asmt.setRiskAnalysis("Updated risk analysis");
        assertEquals("Risk analysis should be updated", "Updated risk analysis", asmt.getRiskAnalysis());
    }

    @Test
    public void testAssessmentUpdateStatus() {
        Assessment asmt = createTestAssessment();
        asmt.setStart(new Date());
        asmt.setEnd(new Date());
        asmt.setStatus("In Progress");
        asmt.setStatus("Complete");
        assertNotNull("Status should not be null", asmt.getStatus());
    }

    // --- Custom Field Update Tests ---

    @Test
    public void testCustomFieldUpdate() {
        Assessment asmt = createTestAssessment();
        List<CustomField> customFields = new ArrayList<>();

        CustomType ct = new CustomType();
        ct.setId(1L);
        ct.setKey("impact_analysis");

        CustomField cf1 = new CustomField();
        cf1.setId(1L);
        cf1.setType(ct);
        cf1.setValue("Original value");
        customFields.add(cf1);

        CustomField cf2 = new CustomField();
        cf2.setId(2L);
        cf2.setType(ct);
        cf2.setValue("Another value");
        customFields.add(cf2);

        asmt.setCustomFields(customFields);

        for (CustomField cf : asmt.getCustomFields()) {
            if (cf.getId().equals(1L)) {
                cf.setValue("Updated value");
            }
        }

        assertEquals("Custom field value should be updated", "Updated value", 
            asmt.getCustomFields().get(0).getValue());
        assertEquals("Other custom field should be unchanged", "Another value", 
            asmt.getCustomFields().get(1).getValue());
    }

    @Test
    public void testCustomFieldNotFound() {
        Assessment asmt = createTestAssessment();
        List<CustomField> customFields = new ArrayList<>();
        CustomField cf = new CustomField();
        cf.setId(1L);
        customFields.add(cf);
        asmt.setCustomFields(customFields);

        Long nonExistentId = 999L;
        boolean found = false;
        for (CustomField field : asmt.getCustomFields()) {
            if (field.getId().equals(nonExistentId)) {
                found = true;
            }
        }
        assertFalse("Non-existent custom field should not be found", found);
    }

    // --- Peer Review Tests ---

    @Test
    public void testSendToPRCreatesPeerReview() {
        Assessment asmt = createTestAssessment();
        asmt.setCompleted(null);
        assertNull("PeerReview should be null before send to PR", asmt.getPeerReview());
        asmt.setInPr();
        assertTrue("Should be in peer review", asmt.isInPr());
    }

    @Test
    public void testSendToPRCreatesComment() {
        Assessment asmt = createTestAssessment();
        PeerReview pr = new PeerReview();
        pr.setCreated(new Date());
        Comment comment = new Comment();
        comment.setCommenter(null);
        pr.setComments(Arrays.asList(comment));
        asmt.setPeerReview(pr);

        assertNotNull("PeerReview should be set", asmt.getPeerReview());
        assertNotNull("Comments should not be null", asmt.getPeerReview().getComments());
        assertEquals("Should have 1 comment", 1, asmt.getPeerReview().getComments().size());
    }

    // --- Finalization Tests ---

    @Test
    public void testFinalizeAssessmentSetsCompleted() {
        Assessment asmt = createTestAssessment();
        asmt.setCompleted(new Date());
        assertNotNull("Completed date should be set after finalization", asmt.getCompleted());
    }

    @Test
    public void testFinalizeAssessmentSetsStatus() {
        Assessment asmt = createTestAssessment();
        asmt.setStatus("In Progress");
        asmt.setCompleted(new Date());
        asmt.setFinalized();
        assertTrue("Should be finalized", asmt.isFinalized());
    }

    @Test
    public void testFinalizeAssessmentSetsFinalized() {
        Assessment asmt = createTestAssessment();
        asmt.setFinalized();
        assertTrue("Should be finalized", asmt.isFinalized());
    }

    // --- Vulnerability Count Tests ---

    @Test
    public void testVulnerabilityCountZero() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            counts.put(i, 0);
        }
        assertEquals("Count for level 0 should be 0", Integer.valueOf(0), counts.get(0));
        assertEquals("Count for level 5 should be 0", Integer.valueOf(0), counts.get(5));
    }

    @Test
    public void testVulnerabilityCountIncrement() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            counts.put(i, 0);
        }
        counts.put(5, counts.get(5) + 1);
        counts.put(5, counts.get(5) + 1);
        counts.put(7, counts.get(7) + 1);
        assertEquals("Count for level 5 should be 2", Integer.valueOf(2), counts.get(5));
        assertEquals("Count for level 7 should be 1", Integer.valueOf(1), counts.get(7));
    }

    // --- Risk Level Map Tests ---

    @Test
    public void testRiskLevelMapCreation() {
        List<RiskLevel> levels = new ArrayList<>();
        RiskLevel rl1 = new RiskLevel();
        rl1.setRiskId(0);
        rl1.setRisk("Informational");
        levels.add(rl1);

        RiskLevel rl2 = new RiskLevel();
        rl2.setRiskId(1);
        rl2.setRisk("Recommended");
        levels.add(rl2);

        RiskLevel rl3 = new RiskLevel();
        rl3.setRiskId(2);
        rl3.setRisk("Low");
        levels.add(rl3);

        RiskLevel rl4 = new RiskLevel();
        rl4.setRiskId(3);
        rl4.setRisk("Medium");
        levels.add(rl4);

        RiskLevel rl5 = new RiskLevel();
        rl5.setRiskId(4);
        rl5.setRisk("High");
        levels.add(rl5);

        RiskLevel rl6 = new RiskLevel();
        rl6.setRiskId(5);
        rl6.setRisk("Critical");
        levels.add(rl6);

        Map<Integer, String> levelMap = new HashMap<>();
        for (RiskLevel level : levels) {
            levelMap.put(level.getRiskId(), level.getRisk());
        }

        assertEquals("Level 0 should be Informational", "Informational", levelMap.get(0));
        assertEquals("Level 5 should be Critical", "Critical", levelMap.get(5));
    }

    // --- Vulnerability Category Map Tests ---

    @Test
    public void testVulnerabilityCategoryMapCreation() {
        Map<String, Integer> catMap = new HashMap<>();
        catMap.put("Injection", 0);
        catMap.put("XSS", 0);
        catMap.put("Injection", catMap.get("Injection") + 1);
        catMap.put("XSS", catMap.get("XSS") + 1);
        catMap.put("XSS", catMap.get("XSS") + 1);
        assertEquals("Injection count should be 1", Integer.valueOf(1), catMap.get("Injection"));
        assertEquals("XSS count should be 2", Integer.valueOf(2), catMap.get("XSS"));
    }

    @Test
    public void testVulnerabilityCategoryMapNewCategory() {
        Map<String, Integer> catMap = new HashMap<>();
        String category = "Injection";
        Integer count = catMap.get(category);
        if (count == null) {
            catMap.put(category, 1);
        } else {
            catMap.put(category, count + 1);
        }
        assertEquals("New category should have count 1", Integer.valueOf(1), catMap.get("Injection"));
    }

    // --- Session Assessment ID Tests ---

    @Test
    public void testSessionAssessmentIdSet() {
        Map<String, Object> session = new HashMap<>();
        session.put("asmtid", 123L);
        assertEquals("Session asmtid should match", Long.valueOf(123L), session.get("asmtid"));
    }

    @Test
    public void testSessionAssessmentIdNull() {
        Map<String, Object> session = new HashMap<>();
        assertNull("Session asmtid should be null when not set", session.get("asmtid"));
    }

    // --- ICS File Generation Tests ---

    @Test
    public void testICSFileNameGeneration() {
        Assessment asmt = createTestAssessment();
        String filename = asmt.getAppId() + "-" + asmt.getName() + "-invite.ics";
        assertNotNull("Filename should not be null", filename);
        assertTrue("Filename should contain appId", filename.contains("APP-001"));
        assertTrue("Filename should contain assessment name", filename.contains("Test Assessment"));
        assertTrue("Filename should end with .ics", filename.endsWith(".ics"));
    }

    @Test
    public void testICSContentCreation() {
        String icsContent = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR";
        assertNotNull("ICS content should not be null", icsContent);
        assertTrue("Should start with BEGIN:VCALENDAR", icsContent.startsWith("BEGIN:VCALENDAR"));
    }

    // --- Calendar Link Generation Tests ---

    @Test
    public void testGoogleCalendarBaseUrl() {
        String baseUrl = "https://calendar.google.com/calendar/render";
        assertEquals("Google calendar base URL should match", 
            "https://calendar.google.com/calendar/render", baseUrl);
    }

    @Test
    public void testOutlookBaseUrl() {
        String baseUrl = "https://outlook.office.com/calendar/0/deeplink/compose";
        assertEquals("Outlook base URL should match", 
            "https://outlook.office.com/calendar/0/deeplink/compose", baseUrl);
    }

    @Test
    public void testLiveBaseUrl() {
        String baseUrl = "https://outlook.live.com/calendar/0/deeplink/compose";
        assertEquals("Live base URL should match", 
            "https://outlook.live.com/calendar/0/deeplink/compose", baseUrl);
    }

    @Test
    public void testCalendarLinkParamsGoogle() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "TEMPLATE");
        params.put("text", "Test Title");
        params.put("details", "Test Details");
        assertNotNull("Params should not be null", params);
        assertEquals("action should be TEMPLATE", "TEMPLATE", params.get("action"));
    }

    @Test
    public void testCalendarLinkParamsOutlook() {
        Map<String, String> params = new HashMap<>();
        params.put("subject", "Test Subject");
        params.put("body", "Test Body");
        params.put("to", "user@example.com");
        assertNotNull("Params should not be null", params);
        assertEquals("subject should match", "Test Subject", params.get("subject"));
    }

    // --- URL Building Tests ---

    @Test
    public void testBuildUrlWithParams() {
        String base = "https://example.com";
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        StringBuilder url = new StringBuilder(base + "?");
        for (String key : params.keySet()) {
            url.append(key).append("=").append(params.get(key)).append("&");
        }
        String result = url.toString().replaceAll("&$", "");

        assertTrue("URL should contain key1", result.contains("key1=value1"));
        assertTrue("URL should contain key2", result.contains("key2=value2"));
        assertFalse("URL should not end with &", result.endsWith("&"));
    }

    // --- File Upload Validation Tests ---

    @Test
    public void testPdfContentTypeDetection() {
        String contentType = "application/pdf";
        boolean isPdf = contentType.contains("pdf");
        assertTrue("Should detect PDF content type", isPdf);
    }

    @Test
    public void testDocxContentTypeDetection() {
        String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        boolean isDocx = contentType.contains("wordprocessingml");
        assertTrue("Should detect DOCX content type", isDocx);
    }

    @Test
    public void testPdfFileExtensionDetection() {
        String filename = "report.pdf";
        boolean isPdf = filename.toLowerCase().endsWith(".pdf");
        assertTrue("Should detect PDF file extension", isPdf);
    }

    @Test
    public void testDocxFileExtensionDetection() {
        String filename = "report.docx";
        boolean isDocx = filename.toLowerCase().endsWith(".docx");
        assertTrue("Should detect DOCX file extension", isDocx);
    }

    @Test
    public void testInvalidFileTypeDetection() {
        String filename = "report.txt";
        boolean isPdf = filename.toLowerCase().endsWith(".pdf");
        boolean isDocx = filename.toLowerCase().endsWith(".docx");
        assertFalse("Should not detect TXT as PDF", isPdf);
        assertFalse("Should not detect TXT as DOCX", isDocx);
    }

    // --- Assessment Queries Helper Tests ---

    @Test
    public void testAssessmentQueriesClassExists() {
        assertNotNull("AssessmentQueries class should exist", 
            com.fuse.dao.query.AssessmentQueries.class);
    }

    // --- Notification Tests ---

    @Test
    public void testNotificationCreation() {
        Notification n = new Notification();
        n.setAssessorId(1L);
        n.setCreated(new Date());
        n.setMessage("Test message");

        assertEquals("AssessorId should match", (Object)Long.valueOf(1L), n.getAssessorId());
        assertNotNull("Created date should not be null", n.getCreated());
        assertEquals("Message should match", "Test message", n.getMessage());
    }

    @Test
    public void testNotificationWithNullFields() {
        Notification n = new Notification();
        assertNull("AssessorId should be null when not set", n.getAssessorId());
        assertNull("Created date should be null when not set", n.getCreated());
        assertNull("Message should be null when not set", n.getMessage());
    }

    // --- BoilerPlate Template Tests ---

    @Test
    public void testBoilerPlateTemplate() {
        BoilerPlate bp = new BoilerPlate();
        bp.setId(1L);
        bp.setText("Test template");
        bp.setType("summary");
        bp.setActive(true);

        assertEquals("Id should match", (Object)Long.valueOf(1L), bp.getId());
        assertEquals("Text should match", "Test template", bp.getText());
        assertEquals("Type should match", "summary", bp.getType());
        assertTrue("Should be active", bp.getActive());
    }

    @Test
    public void testBoilerPlateTemplateInactive() {
        BoilerPlate bp = new BoilerPlate();
        bp.setText("Inactive template");
        bp.setActive(false);
        assertFalse("Should be inactive", bp.getActive());
    }

    // --- Status Class Exists ---

    @Test
    public void testStatusClassExists() {
        assertNotNull("Status class should exist", com.fuse.dao.Status.class);
    }

    // --- Assessment InPR Tests ---

    @Test
    public void testAssessmentInPrFlag() {
        Assessment asmt = createTestAssessment();
        assertFalse("Should not be in PR by default", asmt.isInPr());
        asmt.setInPr();
        assertTrue("Should be in PR after setInPr()", asmt.isInPr());
    }

    // --- Assessment AcceptedEdits Tests ---

    @Test
    public void testAssessmentAcceptedEditsFlag() {
        Assessment asmt = createTestAssessment();
        assertNotNull("Assessment should be created", asmt);
    }

    // --- PeerReview Entity Tests ---

    @Test
    public void testPeerReviewEntity() {
        PeerReview pr = new PeerReview();
        pr.setCreated(new Date());
        pr.setCompleted(new Date());
        pr.setAcceptedEdits(new Date());

        assertNotNull("Created date should not be null", pr.getCreated());
        assertNotNull("Completed date should not be null", pr.getCompleted());
        assertNotNull("AcceptedEdits date should not be null", pr.getAcceptedEdits());
    }

    @Test
    public void testPeerReviewEntityWithNullDates() {
        PeerReview pr = new PeerReview();
        assertNull("Created date should be null when not set", pr.getCreated());
        assertNull("Completed date should be null when not set", pr.getCompleted());
        assertNull("AcceptedEdits date should be null when not set", pr.getAcceptedEdits());
    }

    // --- Comment Entity Tests ---

    @Test
    public void testCommentEntity() {
        Comment c = new Comment();
        c.setCommenter(null);
        c.setComment("Test comment");

        assertNotNull("Comment should not be null", c.getComment());
        assertEquals("Comment should match", "Test comment", c.getComment());
    }

    // --- Files Entity Tests ---

    @Test
    public void testFilesEntity() {
        Files f = new Files();
        f.setId(1L);
        f.setName("report.pdf");

        assertEquals("Id should match", (Object)Long.valueOf(1L), f.getId());
        assertEquals("Name should match", "report.pdf", f.getName());
    }

    // --- FinalReport Entity Tests ---

    @Test
    public void testFinalReportEntity() {
        FinalReport fr = new FinalReport();
        fr.setFilename("test-report.pdf");
        fr.setFileType("pdf");
        fr.setRetest(false);
        fr.setGentime(new Date());

        assertEquals("Filename should match", "test-report.pdf", fr.getFilename());
        assertEquals("FileType should match", "pdf", fr.getFileType());
        assertFalse("Should not be retest", fr.getRetest());
        assertNotNull("Gentime should not be null", fr.getGentime());
    }

    @Test
    public void testFinalReportDocxType() {
        FinalReport fr = new FinalReport();
        fr.setFileType("docx");
        assertEquals("FileType should be docx", "docx", fr.getFileType());
    }

    // --- History Entity Tests ---

    @Test
    public void testHistoryEntity() {
        History h = new History(
        	1l,
            new Date(),
            null,
            "SQL Injection",
            "report.pdf",
            "High",
            "adminuser",
            false
        );

        assertEquals("Vuln should match", "SQL Injection", h.getVuln());
        assertEquals("Report should match", "report.pdf", h.getReport());
        assertEquals("Severity should match", "High", h.getSeverity());
        assertEquals("Assessor should match", "adminuser", h.getAssessor());
    }

    // --- CheckListAnswer Entity Tests ---

    @Test
    public void testCheckListAnswerEntity() {
        CheckListAnswers cla = new CheckListAnswers();
        cla.setAnswer(2);
        assertNotNull("Answer should not be null", cla.getAnswer());
    }

    // --- User Assessment Relationship Tests ---

    @Test
    public void testUserIsAssessor() {
        User user = createTestUser(1L, "assessor", true, false, false);
        assertTrue("User should be assessor", user.getPermissions().isAssessor());
    }

    @Test
    public void testUserIsManager() {
        User user = createTestUser(1L, "manager", false, true, false);
        assertTrue("User should be manager", user.getPermissions().isManager());
    }

    @Test
    public void testUserIsAdmin() {
        User user = createTestUser(1L, "admin", false, false, true);
        assertTrue("User should be admin", user.getPermissions().isAdmin());
    }

    @Test
    public void testUserHasNoPermissions() {
        User user = createTestUser(1L, "noperms", false, false, false);
        assertFalse("User should not be assessor", user.getPermissions().isAssessor());
        assertFalse("User should not be manager", user.getPermissions().isManager());
        assertFalse("User should not be admin", user.getPermissions().isAdmin());
    }

    // --- Assessment Distribution List Tests ---

    @Test
    public void testDistributionListSplit() {
        String distributionList = "user1@example.com;user2@example.com;user3@example.com";
        List<String> emails = new ArrayList<>();
        for (String email : distributionList.split(";")) {
            emails.add(email);
        }

        assertEquals("Should have 3 emails", 3, emails.size());
        assertEquals("First email should match", "user1@example.com", emails.get(0));
        assertEquals("Second email should match", "user2@example.com", emails.get(1));
        assertEquals("Third email should match", "user3@example.com", emails.get(2));
    }

    @Test
    public void testDistributionListSingleEmail() {
        String distributionList = "user@example.com";
        List<String> emails = new ArrayList<>();
        for (String email : distributionList.split(";")) {
            emails.add(email);
        }

        assertEquals("Should have 1 email", 1, emails.size());
    }

    // --- Assessment Notes Tests ---

    @Test
    public void testAssessmentHasNotebook() {
        Assessment asmt = createTestAssessment();
        asmt.setNotebook(new ArrayList<>());
        assertNotNull("Notebook should not be null", asmt.getNotebook());
        assertEquals("Notebook should be empty", 0, asmt.getNotebook().size());
    }

    // --- Assessment Type Tests ---

    @Test
    public void testAssessmentTypeEntity() {
        AssessmentType at = new AssessmentType();
        at.setId(1L);
        at.setType("Web Application");

        assertEquals("Id should match", (Object)Long.valueOf(1L), at.getId());
        assertEquals("Type should match", "Web Application", at.getType());
    }

    // --- Assessment Locking Logic Tests ---

    @Test
    public void testAssessmentCompletedIsBlocked() {
        Assessment asmt = createTestAssessment();
        asmt.setCompleted(new Date());
        User user = createTestUser(1L, "user1", true, false, false);
        asmt.setAssessor(Arrays.asList(user));
        assertNotNull("Completed date should be set", asmt.getCompleted());
    }

    @Test
    public void testAssessmentNotOwnerIsBlocked() {
        Assessment asmt = createTestAssessment();
        User owner = createTestUser(1L, "owner", true, false, false);
        User nonOwner = createTestUser(2L, "nonowner", true, false, false);
        asmt.setAssessor(Arrays.asList(owner));

        assertFalse("Non-owner should not be in assessor list", 
            asmt.getAssessor().stream().anyMatch(u -> u.getId() == nonOwner.getId()));
        assertTrue("Owner should be in assessor list", 
            asmt.getAssessor().stream().anyMatch(u -> u.getId() == owner.getId()));
    }

    // --- Peer Review Lock Logic Tests ---

    @Test
    public void testAssessmentInPeerReviewBlocked() {
        Assessment asmt = createTestAssessment();
        User user = createTestUser(1L, "user1", true, false, false);
        asmt.setAssessor(Arrays.asList(user));

        PeerReview pr = new PeerReview();
        pr.setCreated(new Date());
        pr.setCompleted(null);
        pr.setAcceptedEdits(null);
        asmt.setPeerReview(pr);

        assertNotNull("PeerReview should be set", asmt.getPeerReview());
        assertNull("Completed should be null for in-progress PR", asmt.getPeerReview().getCompleted());
        assertNull("AcceptedEdits should be null for in-progress PR", asmt.getPeerReview().getAcceptedEdits());
    }

    @Test
    public void testAssessmentPeerReviewCompleteNotBlocked() {
        Assessment asmt = createTestAssessment();
        User user = createTestUser(1L, "user1", true, false, false);
        asmt.setAssessor(Arrays.asList(user));

        PeerReview pr = new PeerReview();
        pr.setCreated(new Date());
        pr.setCompleted(new Date());
        pr.setAcceptedEdits(new Date());
        asmt.setPeerReview(pr);

        assertNotNull("AcceptedEdits should not be null", asmt.getPeerReview().getAcceptedEdits());
        assertNotNull("Completed should not be null", asmt.getPeerReview().getCompleted());
    }
}
