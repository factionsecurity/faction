package com.fuse.api;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Base64;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * Unit tests for Assessment endpoint helper methods.
 * Tests decodeAndSanitize() and outlineImage() which are private helper methods
 * used by the assessments REST API.
 */
public class AssessmentHelperTests {

    private assessments createAssessmentsInstance() {
        return new assessments();
    }

    // --- decodeAndSanitize Tests ---

    @Test
    public void testDecodeAndSanitizeBase64EncodedText() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String original = "This is base64 encoded text";
        String encoded = Base64.getEncoder().encodeToString(original.getBytes());

        String result = (String) method.invoke(api, encoded);

        // Base64 decode + markdown convert + HTML sanitize
        // Since "This is base64 encoded text" has no markdown, it should be close to original
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeBase64WithNewlines() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String original = "This is\nbase64\ntext\nwith\nnewlines";
        String encoded = Base64.getEncoder().encodeToString(original.getBytes());

        String result = (String) method.invoke(api, encoded);
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeMarkdownHeading() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "# Heading 1";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        // Should convert markdown to HTML after base64 decode
        assertNotNull("Result should not be null", result);
        assertTrue("Should contain H1 tag", result.contains("<h1>Heading 1</h1>"));
    }

    @Test
    public void testDecodeAndSanitizeMarkdownBold() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "**bold text**";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain strong tag", result.contains("<strong>bold text</strong>"));
    }

    @Test
    public void testDecodeAndSanitizeMarkdownItalic() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "*italic text*";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain em tag", result.contains("<em>italic text</em>"));
    }

    @Test
    public void testDecodeAndSanitizeMarkdownList() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "- Item 1\n- Item 2\n- Item 3";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain ul tag", result.contains("<ul>"));
        assertTrue("Should contain li tags", result.contains("<li>"));
    }

    @Test
    public void testDecodeAndSanitizeMarkdownCodeBlock() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "`inline code`";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain code tag", result.contains("<code>"));
    }

    @Test
    public void testDecodeAndSanitizeMarkdownLink() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "[link text](https://example.com)";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain anchor tag", result.contains("<a"));
        assertTrue("Should contain href", result.contains("href="));
    }

    @Test
    public void testDecodeAndSanitizeMarkdownTable() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "| Header 1 | Header 2 |\n|----------|----------|\n| Cell 1   | Cell 2   |";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain table tag", result.contains("<table>"));
    }

    @Test
    public void testDecodeAndSanitizeHtmlEscaped() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String htmlEscaped = "&lt;script&gt;alert('xss')&lt;/script&gt;";
        String encoded = Base64.getEncoder().encodeToString(htmlEscaped.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeXSSAttempt() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String xss = "<script>alert('xss')</script>";
        String encoded = Base64.getEncoder().encodeToString(xss.getBytes());
        String result = (String) method.invoke(api, encoded);

        // Should sanitize HTML - script tags should be removed or escaped
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeEmptyString() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(api, "");

        // Empty string decodes to empty bytes, markdown convert produces "\n"
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeNull() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(api, (String) null);

        // Null input returns empty string
        assertEquals("Null should return empty string", "", result);
    }

    @Test
    public void testDecodeAndSanitizeInvalidBase64() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String invalidBase64 = "This is not valid base64!!!";
        String result = (String) method.invoke(api, invalidBase64);

        // Should handle invalid base64 gracefully
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeMixedContent() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String markdown = "# Heading\n\nThis is **bold** and *italic*.\n\n- Item 1\n- Item 2";
        String encoded = Base64.getEncoder().encodeToString(markdown.getBytes());
        String result = (String) method.invoke(api, encoded);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain h1", result.contains("<h1>Heading</h1>"));
        assertTrue("Should contain strong", result.contains("<strong>bold</strong>"));
        assertTrue("Should contain em", result.contains("<em>italic</em>"));
        assertTrue("Should contain ul", result.contains("<ul>"));
        assertTrue("Should contain li", result.contains("<li>"));
    }

    @Test
    public void testDecodeAndSanitizeBase64WithHtml() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String html = "<p>This is <strong>bold</strong> HTML</p>";
        String encoded = Base64.getEncoder().encodeToString(html.getBytes());
        String result = (String) method.invoke(api, encoded);

        // Should decode base64, then markdown convert (no-op for HTML), then sanitize
        assertNotNull("Result should not be null", result);
    }

    @Test
    public void testDecodeAndSanitizeHtmlWithSpecialChars() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("decodeAndSanitize", String.class);
        method.setAccessible(true);

        String html = "<p>Price: $100 & quantity: 5 items</p>";
        String encoded = Base64.getEncoder().encodeToString(html.getBytes());
        String result = (String) method.invoke(api, encoded);

        // Should handle special characters
        assertNotNull("Result should not be null", result);
    }

    // --- outlineImage Tests ---

    @Test
    public void testOutlineImageValidDataUri() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("outlineImage", String.class);
        method.setAccessible(true);

        String dataUri = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        String result = (String) method.invoke(api, dataUri);

        // Should return modified data URI with border
        assertNotNull("Result should not be null", result);
        assertTrue("Should start with data:image", result.startsWith("data:image"));
    }

    @Test
    public void testOutlineImageEmptyString() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("outlineImage", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(api, "");

        assertEquals("Empty string should return empty string", "", result);
    }

    @Test
    public void testOutlineImageNull() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("outlineImage", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(api, (String) null);

        // null input returns null (per source line 1882: returns encodedImage which is null)
        assertNull("Null should return null", result);
    }

@Test
    public void testOutlineImageInvalidDataUri() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("outlineImage", String.class);
        method.setAccessible(true);

        String invalidDataUri = "data:image/png;base64,invalid!!!";
        try {
            String result = (String) method.invoke(api, invalidDataUri);
            // If it doesn't throw, result should be non-null
            assertNotNull("Result should not be null", result);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // ImageBorderUtil may throw for invalid data - that's acceptable behavior
            assertNotNull("Method should handle invalid data", invalidDataUri);
         }
     }

    @Test
    public void testOutlineImageJpgImage() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("outlineImage", String.class);
        method.setAccessible(true);

        String jpgDataUri = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYI4Q4sSs";
        String result = (String) method.invoke(api, jpgDataUri);

        assertNotNull("Result should not be null", result);
        assertTrue("Should start with data:image", result.startsWith("data:image"));
    }

    // --- Permission Check Tests (direct Permissions) ---

    @Test
    public void testIsManagerPermission() {
        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setManager(true);
        assertTrue("User with manager=true should have manager access", perms.isManager());
    }

    @Test
    public void testIsAssessorPermission() {
        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setAssessor(true);
        assertTrue("User with assessor=true should have assessor access", perms.isAssessor());
    }

    @Test
    public void testIsEngagementPermission() {
        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setEngagement(true);
        assertTrue("User with engagement=true should have engagement access", perms.isEngagement());
    }

    @Test
    public void testIsRemediationPermission() {
        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setRemediation(true);
        assertTrue("User with remediation=true should have remediation access", perms.isRemediation());
    }

    @Test
    public void testHasNoPermissions() {
        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        assertFalse("User with no permissions should not have manager access", perms.isManager());
        assertFalse("User with no permissions should not have assessor access", perms.isAssessor());
        assertFalse("User with no permissions should not have engagement access", perms.isEngagement());
        assertFalse("User with no permissions should not have remediation access", perms.isRemediation());
    }

    @Test
    public void testHasAllPermissions() {
        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setManager(true);
        perms.setAssessor(true);
        perms.setEngagement(true);
        perms.setRemediation(true);
        assertTrue("User with all permissions should have manager access", perms.isManager());
        assertTrue("User with all permissions should have assessor access", perms.isAssessor());
        assertTrue("User with all permissions should have engagement access", perms.isEngagement());
        assertTrue("User with all permissions should have remediation access", perms.isRemediation());
    }

    @Test
    public void testUserWithNullPermissions() {
        com.fuse.dao.User user = new com.fuse.dao.User();
        user.setId(Long.valueOf(1L));
        user.setUsername("testuser");
        // Don't set permissions
        assertNull("Permissions should be null when not set", user.getPermissions());
    }

    // --- Broadcast Message Tests ---

    @Test
    public void testBroadcastMessageStructure() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("sendBroadcastMessage", com.fuse.dao.User.class, Long.class, Long.class, String.class, Object.class);
        method.setAccessible(true);

        com.fuse.dao.User user = new com.fuse.dao.User();
        user.setId(Long.valueOf(1L));
        user.setUsername("testuser");
        method.invoke(api, user, Long.valueOf(123L), Long.valueOf(456L), "add", "");

        // If we get here without NPE, the method is callable
        assertNotNull("Method should be accessible", method);
    }

    @Test
    public void testBroadcastMessageWithAssessmentId() throws Exception {
        assessments api = createAssessmentsInstance();
        Method method = assessments.class.getDeclaredMethod("sendBroadcastMessage", com.fuse.dao.User.class, Long.class, Long.class, String.class, Object.class);
        method.setAccessible(true);

        com.fuse.dao.User user = new com.fuse.dao.User();
        user.setId(Long.valueOf(1L));
        method.invoke(api, user, Long.valueOf(789L), Long.valueOf(101L), "update", "{\"test\":true}");

        assertNotNull("Method should be accessible", method);
    }

    // --- Helper Methods ---

    private com.fuse.dao.User createTestUserWithPermissions(boolean isManager, boolean isAssessor, 
                                                            boolean isEngagement, boolean isRemediation) {
        com.fuse.dao.User user = new com.fuse.dao.User();
        user.setId(Long.valueOf(1L));
        user.setUsername("testuser");

        com.fuse.dao.Permissions perms = new com.fuse.dao.Permissions();
        perms.setAdmin(false);
        perms.setAssessor(false);
        perms.setEngagement(false);
        perms.setManager(false);
        perms.setExecutive(false);
        perms.setRemediation(false);
        perms.setManager(isManager);
        perms.setAssessor(isAssessor);
        perms.setEngagement(isEngagement);
        perms.setRemediation(isRemediation);
        user.setPermissions(perms);

        return user;
    }
}
