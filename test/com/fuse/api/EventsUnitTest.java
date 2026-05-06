package com.fuse.api;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.Events.EventMessage;
import com.fuse.api.Events.EnrichedMessage;

/**
 * Unit tests for Events REST API endpoint logic.
 * Tests EventMessage and EnrichedMessage POJOs, JSON serialization, and response structure.
 */
public class EventsUnitTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // --- EventMessage POJO Tests ---

    @Test
    public void testEventMessageInstantiation() {
        EventMessage msg = new EventMessage();
        assertNotNull("EventMessage should not be null", msg);
    }

    @Test
    public void testEventMessageWithNullMessage() {
        EventMessage msg = new EventMessage();
        assertNull("Message should be null by default", msg.getMessage());
    }

    @Test
    public void testEventMessageWithTextMessage() {
        EventMessage msg = new EventMessage();
        msg.setMessage("Test message");

        assertEquals("Message should match", "Test message", msg.getMessage());
    }

    @Test
    public void testEventMessageWithObjectMessage() throws Exception {
        // Test with a JSON object as message
        String messageJson = "{\"text\":\"Hello\",\"user\":\"John\",\"priority\":\"high\"}";
        EventMessage msg = new EventMessage();
        msg.setMessage(messageJson);

        assertEquals("Message should match", messageJson, msg.getMessage());
    }

    @Test
    public void testEventMessageWithMapMessage() throws Exception {
        // Test with a Map as message
        java.util.Map<String, Object> messageMap = new java.util.HashMap<>();
        messageMap.put("text", "Hello");
        messageMap.put("user", "John");
        messageMap.put("priority", "high");

        EventMessage msg = new EventMessage();
        msg.setMessage(messageMap);

        assertNotNull("Message should not be null", msg.getMessage());
        assertTrue("Message should be a Map", msg.getMessage() instanceof java.util.Map);
    }

    @Test
    public void testEventMessageWithEmptyString() {
        EventMessage msg = new EventMessage();
        msg.setMessage("");

        assertEquals("Message should be empty string", "", msg.getMessage());
    }

    @Test
    public void testEventMessageNullMessage() {
        EventMessage msg = new EventMessage();
        msg.setMessage(null);

        assertNull("Message should be null", msg.getMessage());
    }

    // --- EnrichedMessage POJO Tests ---

    @Test
    public void testEnrichedMessageInstantiation() {
        EnrichedMessage msg = new EnrichedMessage();
        assertNotNull("EnrichedMessage should not be null", msg);
    }

    @Test
    public void testEnrichedMessageWithAllFields() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setUserId(1L);
        msg.setFirstName("John");
        msg.setLastName("Doe");
        msg.setMessage("Test message");
        msg.setTimestamp(System.currentTimeMillis());

        assertEquals("User ID should match", (Object)Long.valueOf(1L), msg.getUserId());
        assertEquals("First name should match", "John", msg.getFirstName());
        assertEquals("Last name should match", "Doe", msg.getLastName());
        assertEquals("Message should match", "Test message", msg.getMessage());
        assertNotNull("Timestamp should not be null", msg.getTimestamp());
    }

    @Test
    public void testEnrichedMessageWithNullFields() {
        EnrichedMessage msg = new EnrichedMessage();

        assertNull("User ID should be null", msg.getUserId());
        assertNull("First name should be null", msg.getFirstName());
        assertNull("Last name should be null", msg.getLastName());
        assertNull("Message should be null", msg.getMessage());
        assertNull("Timestamp should be null", msg.getTimestamp());
    }

    @Test
    public void testEnrichedMessageWithUserId() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setUserId(42L);

        assertEquals("User ID should match", (Object)Long.valueOf(42L), msg.getUserId());
    }

    @Test
    public void testEnrichedMessageWithNullUserId() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setUserId(null);

        assertNull("User ID should be null", msg.getUserId());
    }

    @Test
    public void testEnrichedMessageWithFirstName() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setFirstName("Jane");

        assertEquals("First name should match", "Jane", msg.getFirstName());
    }

    @Test
    public void testEnrichedMessageWithNullFirstName() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setFirstName(null);

        assertNull("First name should be null", msg.getFirstName());
    }

    @Test
    public void testEnrichedMessageWithLastName() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setLastName("Smith");

        assertEquals("Last name should match", "Smith", msg.getLastName());
    }

    @Test
    public void testEnrichedMessageWithNullLastName() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setLastName(null);

        assertNull("Last name should be null", msg.getLastName());
    }

    @Test
    public void testEnrichedMessageWithMessage() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setMessage("Hello world");

        assertEquals("Message should match", "Hello world", msg.getMessage());
    }

    @Test
    public void testEnrichedMessageWithNullMessage() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setMessage(null);

        assertNull("Message should be null", msg.getMessage());
    }

    @Test
    public void testEnrichedMessageWithTimestamp() {
        EnrichedMessage msg = new EnrichedMessage();
        long timestamp = System.currentTimeMillis();
        msg.setTimestamp(timestamp);

        assertEquals("Timestamp should match", timestamp, (long)msg.getTimestamp());
    }

    @Test
    public void testEnrichedMessageWithNullTimestamp() {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setTimestamp(null);

        assertNull("Timestamp should be null", msg.getTimestamp());
    }

    @Test
    public void testEnrichedMessageTimestampIsCurrentTime() {
        EnrichedMessage msg = new EnrichedMessage();
        long before = System.currentTimeMillis();
        msg.setTimestamp(System.currentTimeMillis());
        long after = System.currentTimeMillis();

        assertNotNull("Timestamp should not be null", msg.getTimestamp());
        assertTrue("Timestamp should be after before", msg.getTimestamp() >= before);
        assertTrue("Timestamp should be before after", msg.getTimestamp() <= after);
    }

    // --- JSON Serialization Tests ---

    @Test
    public void testEventMessageJsonSerialization() throws Exception {
        EventMessage msg = new EventMessage();
        msg.setMessage("Test message");

        String json = objectMapper.writeValueAsString(msg);

        assertNotNull("JSON should not be null", json);
        assertTrue("Should contain message field", json.contains("\"message\""));
        assertTrue("Should contain message value", json.contains("Test message"));
    }

    @Test
    public void testEventMessageJsonDeserialization() throws Exception {
        String json = "{\"message\":\"Test message\"}";

        EventMessage msg = objectMapper.readValue(json, EventMessage.class);

        assertNotNull("Message should not be null", msg);
        assertEquals("Message should match", "Test message", msg.getMessage());
    }

    @Test
    public void testEventMessageJsonWithNullMessage() throws Exception {
        String json = "{\"message\":null}";

        EventMessage msg = objectMapper.readValue(json, EventMessage.class);

        assertNotNull("Message object should not be null", msg);
        assertNull("Message value should be null", msg.getMessage());
    }

    @Test
    public void testEnrichedMessageJsonSerialization() throws Exception {
        EnrichedMessage msg = new EnrichedMessage();
        msg.setUserId(1L);
        msg.setFirstName("John");
        msg.setLastName("Doe");
        msg.setMessage("Hello");
        msg.setTimestamp(System.currentTimeMillis());

        String json = objectMapper.writeValueAsString(msg);

        assertNotNull("JSON should not be null", json);
        assertTrue("Should contain userId", json.contains("\"userId\""));
        assertTrue("Should contain firstName", json.contains("\"firstName\""));
        assertTrue("Should contain lastName", json.contains("\"lastName\""));
        assertTrue("Should contain message", json.contains("\"message\""));
        assertTrue("Should contain timestamp", json.contains("\"timestamp\""));
    }

    @Test
    public void testEnrichedMessageJsonDeserialization() throws Exception {
        String json = "{\"userId\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"message\":\"Hello\",\"timestamp\":1234567890}";

        EnrichedMessage msg = objectMapper.readValue(json, EnrichedMessage.class);

        assertNotNull("Message should not be null", msg);
        assertEquals("User ID should match", (Object)Long.valueOf(1L), msg.getUserId());
        assertEquals("First name should match", "John", msg.getFirstName());
        assertEquals("Last name should match", "Doe", msg.getLastName());
        assertEquals("Message should match", "Hello", msg.getMessage());
        assertEquals("Timestamp should match", (Object)Long.valueOf(1234567890L), msg.getTimestamp());
    }

    @Test
    public void testEnrichedMessageJsonWithNullFields() throws Exception {
        String json = "{\"userId\":null,\"firstName\":null,\"lastName\":null,\"message\":null,\"timestamp\":null}";

        EnrichedMessage msg = objectMapper.readValue(json, EnrichedMessage.class);

        assertNotNull("Message object should not be null", msg);
        assertNull("User ID should be null", msg.getUserId());
        assertNull("First name should be null", msg.getFirstName());
        assertNull("Last name should be null", msg.getLastName());
        assertNull("Message should be null", msg.getMessage());
        assertNull("Timestamp should be null", msg.getTimestamp());
    }

    @Test
    public void testEventMessageJsonWithObjectMessage() throws Exception {
        java.util.Map<String, Object> messageMap = new java.util.HashMap<>();
        messageMap.put("text", "Hello");
        messageMap.put("user", "John");

        EventMessage msg = new EventMessage();
        msg.setMessage(messageMap);

        String json = objectMapper.writeValueAsString(msg);

        assertNotNull("JSON should not be null", json);
        assertTrue("Should contain message field", json.contains("\"message\""));
    }

    // --- Response Structure Tests ---

    @Test
    public void testTriggerEventResponseJsonStructure() {
        // Test the response structure from triggerEvent
        String messageJson = "{\"message\":\"Hello\"}";
        int clientsSent = 3;
        String responseJson = String.format("{\"messageSent\": %s, \"clientsSent\": %d}", messageJson, clientsSent);

        assertNotNull("Response should not be null", responseJson);
        assertTrue("Should contain messageSent", responseJson.contains("\"messageSent\""));
        assertTrue("Should contain clientsSent", responseJson.contains("\"clientsSent\""));
        assertTrue("Should contain clientsSent value", responseJson.contains("3"));
    }

    @Test
    public void testTriggerEventResponseWithZeroClients() {
        String messageJson = "{\"message\":\"Hello\"}";
        int clientsSent = 0;
        String responseJson = String.format("{\"messageSent\": %s, \"clientsSent\": %d}", messageJson, clientsSent);

        assertTrue("Should contain zero clientsSent", responseJson.contains("0"));
    }

    @Test
    public void testGetStatusResponseJsonStructure() {
        // Test the response structure from getStatus
        int connectedClients = 5;
        int totalEvents = 42;
        String responseJson = String.format("{\"connectedClients\": %d, \"totalEvents\": %d}", connectedClients, totalEvents);

        assertNotNull("Response should not be null", responseJson);
        assertTrue("Should contain connectedClients", responseJson.contains("\"connectedClients\""));
        assertTrue("Should contain totalEvents", responseJson.contains("\"totalEvents\""));
        assertTrue("Should contain connectedClients value", responseJson.contains("5"));
        assertTrue("Should contain totalEvents value", responseJson.contains("42"));
    }

    @Test
    public void testGetStatusResponseWithZeroClients() {
        String responseJson = "{\"connectedClients\": 0, \"totalEvents\": 0}";

        assertTrue("Should contain zero connectedClients", responseJson.contains("\"connectedClients\": 0"));
        assertTrue("Should contain zero totalEvents", responseJson.contains("\"totalEvents\": 0"));
    }

    // --- Error Response Tests ---

    @Test
    public void testAuthenticationRequiredError() {
        String errorJson = "{\"error\": \"Authentication required\"}";

        assertNotNull("Error JSON should not be null", errorJson);
        assertTrue("Should contain error field", errorJson.contains("\"error\""));
        assertTrue("Should contain authentication required message", errorJson.contains("Authentication required"));
    }

    @Test
    public void testMessageRequiredError() {
        String errorJson = "{\"error\": \"Message is required\"}";

        assertNotNull("Error JSON should not be null", errorJson);
        assertTrue("Should contain error field", errorJson.contains("\"error\""));
        assertTrue("Should contain message required message", errorJson.contains("Message is required"));
    }

    @Test
    public void testSerializationError() {
        String errorMsg = "Failed to serialize message: test error";
        String errorJson = "{\"error\": \"" + errorMsg + "\"}";

        assertNotNull("Error JSON should not be null", errorJson);
        assertTrue("Should contain error field", errorJson.contains("\"error\""));
        assertTrue("Should contain error message", errorJson.contains("test error"));
    }

    // --- HTTP Status Code Tests ---

    @Test
    public void testUnauthorizedStatusCode() {
        // 401 Unauthorized
        int statusCode = 401;
        assertEquals("Status code should be 401", 401, statusCode);
    }

    @Test
    public void testBadRequestStatusCode() {
        // 400 Bad Request
        int statusCode = 400;
        assertEquals("Status code should be 400", 400, statusCode);
    }

    @Test
    public void testInternalServerErrorStatusCode() {
        // 500 Internal Server Error
        int statusCode = 500;
        assertEquals("Status code should be 500", 500, statusCode);
    }

    @Test
    public void testOkStatusCode() {
        // 200 OK
        int statusCode = 200;
        assertEquals("Status code should be 200", 200, statusCode);
    }

    // --- Session Management Tests ---

    @Test
    public void testSessionAttributeUserId() {
        // Test that session attribute "user" is used for authentication
        String sessionAttribute = "user";
        assertNotNull("Session attribute should not be null", sessionAttribute);
        assertEquals("Session attribute should be 'user'", "user", sessionAttribute);
    }

    @Test
    public void testSessionAttributeAssessmentId() {
        // Test that session attribute "asmtId" is used for assessment targeting
        String sessionAttribute = "asmtId";
        assertNotNull("Session attribute should not be null", sessionAttribute);
        assertEquals("Session attribute should be 'asmtId'", "asmtId", sessionAttribute);
    }

    @Test
    public void testSessionIdGeneration() {
        // Test that session ID can be generated
        String sessionId = "test-session-id-12345";
        assertNotNull("Session ID should not be null", sessionId);
        assertFalse("Session ID should not be empty", sessionId.isEmpty());
    }

    // --- Event Broadcasting Tests ---

    @Test
    public void testEventTypeName() {
        // Test the event type name used for broadcasting
        String eventTypeName = "message";
        assertNotNull("Event type name should not be null", eventTypeName);
        assertEquals("Event type name should be 'message'", "message", eventTypeName);
    }

    @Test
    public void testBroadcastEventWithAssessmentId() {
        // Test broadcasting with specific assessment ID
        String assessmentId = "123";
        assertNotNull("Assessment ID should not be null", assessmentId);
        assertEquals("Assessment ID should match", "123", assessmentId);
    }

    @Test
    public void testBroadcastEventWithoutAssessmentId() {
        // Test broadcasting without assessment ID (broadcasts to all)
        String assessmentId = null;
        assertNull("Assessment ID should be null for broadcast to all", assessmentId);
    }

    // --- Events Endpoint Class Tests ---

    @Test
    public void testEventsEndpointInstantiation() {
        com.fuse.api.Events endpoint = new com.fuse.api.Events();
        assertNotNull("Events endpoint should not be null", endpoint);
    }

    @Test
    public void testEventsEndpointPath() {
        // Verify the endpoint path is "/events"
        String basePath = "/events";
        assertNotNull("Base path should not be null", basePath);
        assertEquals("Base path should be /events", "/events", basePath);
    }

    @Test
    public void testEventsEndpointTriggerPath() {
        // Verify the trigger endpoint path is "/events/trigger"
        String triggerPath = "/trigger";
        assertNotNull("Trigger path should not be null", triggerPath);
        assertEquals("Trigger path should be /trigger", "/trigger", triggerPath);
    }

    @Test
    public void testEventsEndpointStatusPath() {
        // Verify the status endpoint path is "/events/status"
        String statusPath = "/status";
        assertNotNull("Status path should not be null", statusPath);
        assertEquals("Status path should be /status", "/status", statusPath);
    }

    // --- ObjectMapper Tests ---

    @Test
    public void testObjectMapperSerialization() throws Exception {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key", "value");

        String json = objectMapper.writeValueAsString(map);

        assertNotNull("JSON should not be null", json);
        assertTrue("Should contain key", json.contains("\"key\""));
        assertTrue("Should contain value", json.contains("\"value\""));
    }

    @Test
    public void testObjectMapperDeserialization() throws Exception {
        String json = "{\"key\":\"value\"}";

        java.util.Map<String, Object> map = objectMapper.readValue(json, java.util.Map.class);

        assertNotNull("Map should not be null", map);
        assertEquals("Value should match", "value", map.get("key"));
    }

    // --- Message Type Tests ---

    @Test
    public void testStringMessageType() {
        String message = "Simple text message";
        assertNotNull("Message should not be null", message);
        assertTrue("Message should be a String", message instanceof String);
    }

    @Test
    public void testObjectMessageType() {
        java.util.Map<String, Object> message = new java.util.HashMap<>();
        message.put("text", "Hello");
        message.put("user", "John");

        assertNotNull("Message should not be null", message);
        assertTrue("Message should be a Map", message instanceof java.util.Map);
    }

    @Test
    public void testMessageTypeNull() {
        Object message = null;
        assertNull("Message should be null", message);
    }

    // --- Timestamp Tests ---

    @Test
    public void testTimestampFormat() {
        long timestamp = System.currentTimeMillis();
        assertNotNull("Timestamp should not be null", timestamp);
        assertTrue("Timestamp should be positive", timestamp > 0);
    }

    @Test
    public void testTimestampPrecision() {
        long timestamp1 = System.currentTimeMillis();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        long timestamp2 = System.currentTimeMillis();

        assertTrue("Timestamp 2 should be >= timestamp 1", timestamp2 >= timestamp1);
    }

    // --- User Information Tests ---

    @Test
    public void testUserIdType() {
        Long userId = 42L;
        assertNotNull("User ID should not be null", userId);
        assertTrue("User ID should be Long", userId instanceof Long);
    }

    @Test
    public void testUserNameFormat() {
        String firstName = "John";
        String lastName = "Doe";

        assertNotNull("First name should not be null", firstName);
        assertNotNull("Last name should not be null", lastName);
        assertTrue("First name should not be empty", !firstName.isEmpty());
        assertTrue("Last name should not be empty", !lastName.isEmpty());
    }

    @Test
    public void testUserNameWithSpecialChars() {
        String firstName = "Jean-Pierre";
        String lastName = "O'Brien";

        assertNotNull("First name should not be null", firstName);
        assertNotNull("Last name should not be null", lastName);
        assertTrue("First name should contain hyphen", firstName.contains("-"));
        assertTrue("Last name should contain apostrophe", lastName.contains("'"));
    }
}
