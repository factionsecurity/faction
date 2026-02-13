package com.fuse.servlets;

import com.fuse.dao.User;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Server-Side Events (SSE) Servlet
 *
 * Handles SSE connections using Servlet 3.0+ async support.
 * Clients connect to this servlet to receive real-time event streams.
 *
 * Registered in web.xml at /service/events/stream with async-supported=true
 * Requires authenticated session with user object.
 */
public class EventStreamServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Wrapper class to store client connection info
     */
    private static class ClientConnection {
        final AsyncContext asyncContext;
        final String assessmentId;

        ClientConnection(AsyncContext asyncContext, String assessmentId) {
            this.asyncContext = asyncContext;
            this.assessmentId = assessmentId;
        }
    }

    // Thread-safe map to store all connected SSE clients by session ID
    private static final ConcurrentHashMap<String, ClientConnection> CLIENTS = new ConcurrentHashMap<>();

    // Counter for event IDs
    private static final AtomicInteger eventCounter = new AtomicInteger(0);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check authentication - user must be logged in
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            return;
        }

        // Set up SSE response headers
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");

        // Start async processing
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0); // No timeout - keep connection open

        // Use session ID to identify this client
        final String sessionId = request.getSession().getId();

        // Get assessment ID from request parameter or session
        String assessmentId = request.getParameter("assessmentId");
        if (assessmentId == null) {
            // Fallback to session attribute if not in query parameter
            Object asmtId = request.getSession().getAttribute("asmtId");
            assessmentId = (asmtId != null) ? asmtId.toString() : null;
        }
        final String finalAssessmentId = assessmentId;

        // Add this client to the map with assessment context
        CLIENTS.put(sessionId, new ClientConnection(asyncContext, finalAssessmentId));

        // Send welcome message
        try {
            PrintWriter writer = asyncContext.getResponse().getWriter();
            sendEvent(writer, "connection",
                "Connected to SSE stream. Assessment: " + finalAssessmentId + ", Total clients: " + CLIENTS.size());
            writer.flush();
        } catch (IOException e) {
            CLIENTS.remove(sessionId);
            asyncContext.complete();
        }

        // Handle client disconnection
        asyncContext.addListener(new javax.servlet.AsyncListener() {
            @Override
            public void onComplete(javax.servlet.AsyncEvent event) {
                CLIENTS.remove(sessionId);
                System.out.println("Client disconnected. Remaining clients: " + CLIENTS.size());
            }

            @Override
            public void onTimeout(javax.servlet.AsyncEvent event) {
                CLIENTS.remove(sessionId);
                asyncContext.complete();
            }

            @Override
            public void onError(javax.servlet.AsyncEvent event) {
                CLIENTS.remove(sessionId);
                asyncContext.complete();
            }

            @Override
            public void onStartAsync(javax.servlet.AsyncEvent event) {
                // Not used
            }
        });
    }

    /**
     * Broadcast an event to all connected clients
     */
    public static int broadcastEvent(String eventType, String message) {
        return broadcastEvent(eventType, message, null, null);
    }

    /**
     * Broadcast an event to all connected clients except the one with excludeSessionId
     *
     * @param eventType The type of event to send
     * @param message The message payload
     * @param excludeSessionId Session ID to exclude from broadcast (optional)
     * @return Number of clients that successfully received the message
     */
    public static int broadcastEvent(String eventType, String message, String excludeSessionId) {
        return broadcastEvent(eventType, message, excludeSessionId, null);
    }

    /**
     * Broadcast an event to clients viewing a specific assessment, excluding the originating session
     *
     * @param eventType The type of event to send
     * @param message The message payload
     * @param excludeSessionId Session ID to exclude from broadcast (optional)
     * @param assessmentId Assessment ID to filter clients (optional - if null, broadcasts to all)
     * @return Number of clients that successfully received the message
     */
    public static int broadcastEvent(String eventType, String message, String excludeSessionId, String assessmentId) {
        int successCount = 0;

        for (Map.Entry<String, ClientConnection> entry : CLIENTS.entrySet()) {
            String sessionId = entry.getKey();
            ClientConnection connection = entry.getValue();

            // Skip the excluded session
            if (excludeSessionId != null && sessionId.equals(excludeSessionId)) {
                continue;
            }

            // Filter by assessment ID if provided
            if (assessmentId != null && !assessmentId.equals(connection.assessmentId)) {
                continue;
            }

            try {
                PrintWriter writer = connection.asyncContext.getResponse().getWriter();
                sendEvent(writer, eventType, message);
                writer.flush();
                successCount++;
            } catch (Exception e) {
                // Client disconnected, remove it
                CLIENTS.remove(sessionId);
                try {
                    connection.asyncContext.complete();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        return successCount;
    }

    /**
     * Send a single SSE event
     */
    private static void sendEvent(PrintWriter writer, String eventType, String data) {
        String eventId = String.valueOf(eventCounter.incrementAndGet());
        writer.write("id: " + eventId + "\n");
        writer.write("event: " + eventType + "\n");
        writer.write("data: " + data + "\n");
        writer.write("\n");
    }

    /**
     * Get the number of connected clients
     */
    public static int getConnectedClientsCount() {
        return CLIENTS.size();
    }

    /**
     * Get the total number of events sent
     */
    public static int getTotalEventsCount() {
        return eventCounter.get();
    }
}
