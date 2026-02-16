package com.fuse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.dao.User;
import com.fuse.servlets.EventStreamServlet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Server-Side Events (SSE) Proof of Concept Controller
 *
 * This controller works with EventStreamServlet to demonstrate
 * real-time event broadcasting to multiple connected clients.
 *
 * NOTE: The SSE stream endpoint is handled by EventStreamServlet at /service/events/stream
 * This API handles triggering events and checking status.
 * Requires authenticated session.
 */
@Path("/events")
public class Events {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Context
    private HttpServletRequest request;

    /**
     * Trigger endpoint - POST here to broadcast an event to all connected clients
     *
     * Usage: POST /api/events/trigger
     * Body: { "message": "simple text" }
     *   OR: { "message": { "text": "Hello", "user": "John", "priority": "high" } }
     */
    @POST
    @Path("/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerEvent(EventMessage eventMessage) {
        // Check authentication
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Authentication required\"}")
                .build();
        }

        if (eventMessage == null || eventMessage.getMessage() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Message is required\"}")
                .build();
        }

        try {
            // Create enriched message with user information
            EnrichedMessage enrichedMessage = new EnrichedMessage();
            enrichedMessage.setUserId(user.getId());
            enrichedMessage.setFirstName(user.getFname());
            enrichedMessage.setLastName(user.getLname());
            enrichedMessage.setMessage(eventMessage.getMessage());
            enrichedMessage.setTimestamp(System.currentTimeMillis());

            // Convert the enriched message to JSON string for broadcasting
            String messageJson = objectMapper.writeValueAsString(enrichedMessage);

            // Get the session ID of the client making this request
            String sessionId = request.getSession().getId();

            // Get the assessment ID from the session
            Object asmtIdObj = request.getSession().getAttribute("asmtId");
            String assessmentId = (asmtIdObj != null) ? asmtIdObj.toString() : null;

            // Broadcast to clients viewing the same assessment EXCEPT the one making the edit
            int clientsSent = EventStreamServlet.broadcastEvent("message", messageJson, sessionId, assessmentId);

            // Return response with broadcast stats
            String responseJson = String.format(
                "{\"messageSent\": %s, \"clientsSent\": %d}",
                messageJson, clientsSent
            );

            return Response.ok(responseJson).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to serialize message: " + e.getMessage() + "\"}")
                .build();
        }
    }

    /**
     * Status endpoint - check how many clients are currently connected
     *
     * Usage: GET /api/events/status
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        // Check authentication
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Authentication required\"}")
                .build();
        }

        int connectedClients = EventStreamServlet.getConnectedClientsCount();
        int totalEvents = EventStreamServlet.getTotalEventsCount();

        String responseJson = String.format(
            "{\"connectedClients\": %d, \"totalEvents\": %d}",
            connectedClients, totalEvents
        );
        return Response.ok(responseJson).build();
    }

    /**
     * POJO for incoming event messages
     * Accepts either a String or a JSON object for the message field
     */
    public static class EventMessage {
        private Object message;

        public EventMessage() {}

        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }
    }

    /**
     * Enriched message with user information
     * This is what gets broadcast to all connected clients
     */
    public static class EnrichedMessage {
        private Long userId;
        private String firstName;
        private String lastName;
        private Object message;
        private Long timestamp;

        public EnrichedMessage() {}

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
