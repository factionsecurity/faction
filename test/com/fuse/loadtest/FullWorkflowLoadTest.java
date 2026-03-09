package com.fuse.loadtest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Full workflow load test - simulates actual user behavior
 *
 * This test simulates:
 * 1. User login
 * 2. Viewing assessments
 * 3. Opening assessment details
 * 4. Generating reports
 * 5. Checking for updates (polling)
 *
 * Usage:
 *   Configure BASE_URL and credentials
 *   Run: java -cp ... com.fuse.loadtest.FullWorkflowLoadTest
 */
public class FullWorkflowLoadTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int NUM_USERS = 16;
    private static final String USERNAME = "testuser"; // Change to your test user
    private static final String PASSWORD = "testpass";
    private static final Long ASSESSMENT_ID = 1L; // Assessment to test with

    private static AtomicInteger successfulLogins = new AtomicInteger(0);
    private static AtomicInteger failedLogins = new AtomicInteger(0);
    private static AtomicInteger reportsGenerated = new AtomicInteger(0);
    private static AtomicInteger reportsFailed = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Faction Full Workflow Load Test");
        System.out.println("===========================================");
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Users: " + NUM_USERS);
        System.out.println("Assessment ID: " + ASSESSMENT_ID);
        System.out.println("===========================================\n");

        try {
            // Test 1: Concurrent user logins
            System.out.println(">>> TEST 1: Concurrent User Logins");
            testConcurrentLogins();

            // Test 2: Concurrent assessment viewing
            System.out.println("\n>>> TEST 2: Concurrent Assessment Viewing");
            testConcurrentAssessmentViews();

            // Test 3: Concurrent report generation
            System.out.println("\n>>> TEST 3: Concurrent Report Generation");
            testConcurrentReportGeneration();

            // Test 4: Simulate polling (background updates)
            System.out.println("\n>>> TEST 4: Background Polling Simulation");
            testBackgroundPolling();

            // Summary
            System.out.println("\n===========================================");
            System.out.println("Test Summary");
            System.out.println("===========================================");
            System.out.println("Successful Logins: " + successfulLogins.get());
            System.out.println("Failed Logins: " + failedLogins.get());
            System.out.println("Reports Generated: " + reportsGenerated.get());
            System.out.println("Reports Failed: " + reportsFailed.get());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test 1: Concurrent user logins
     */
    private static void testConcurrentLogins() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_USERS);
        CountDownLatch latch = new CountDownLatch(NUM_USERS);

        for (int i = 0; i < NUM_USERS; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    UserSession session = loginUser(USERNAME, PASSWORD, userId);
                    if (session != null) {
                        successfulLogins.incrementAndGet();
                        System.out.println("[User " + userId + "] Login successful");
                    } else {
                        failedLogins.incrementAndGet();
                        System.err.println("[User " + userId + "] Login failed");
                    }
                } catch (Exception e) {
                    failedLogins.incrementAndGet();
                    System.err.println("[User " + userId + "] Login error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    /**
     * Test 2: Concurrent assessment viewing
     */
    private static void testConcurrentAssessmentViews() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_USERS);
        CountDownLatch latch = new CountDownLatch(NUM_USERS);

        for (int i = 0; i < NUM_USERS; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    UserSession session = loginUser(USERNAME, PASSWORD, userId);
                    if (session != null) {
                        viewAssessment(session, ASSESSMENT_ID, userId);
                    }
                } catch (Exception e) {
                    System.err.println("[User " + userId + "] View error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    /**
     * Test 3: Concurrent report generation (main memory test)
     */
    private static void testConcurrentReportGeneration() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_USERS);
        CountDownLatch latch = new CountDownLatch(NUM_USERS);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_USERS; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    UserSession session = loginUser(USERNAME, PASSWORD, userId);
                    if (session != null) {
                        boolean success = generateReport(session, ASSESSMENT_ID, userId);
                        if (success) {
                            reportsGenerated.incrementAndGet();
                        } else {
                            reportsFailed.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    reportsFailed.incrementAndGet();
                    System.err.println("[User " + userId + "] Report error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime - startTime) + "ms");
    }

    /**
     * Test 4: Background polling simulation
     */
    private static void testBackgroundPolling() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_USERS);

        System.out.println("Simulating 30 seconds of background polling...");

        for (int i = 0; i < NUM_USERS; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    UserSession session = loginUser(USERNAME, PASSWORD, userId);
                    if (session != null) {
                        // Poll every 5 seconds for 30 seconds (6 polls)
                        for (int poll = 0; poll < 6; poll++) {
                            checkForUpdates(session, ASSESSMENT_ID, userId);
                            Thread.sleep(5000);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[User " + userId + "] Polling error: " + e.getMessage());
                }
            });
        }

        Thread.sleep(35000); // Wait for polling to complete
        executor.shutdown();
    }

    /**
     * Login user and get session
     */
    private static UserSession loginUser(String username, String password, int userId) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(BASE_URL + "/Login");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        post.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() == 200) {
            String sessionId = extractSessionId(response);
            return new UserSession(userId, sessionId, client);
        }

        return null;
    }

    /**
     * View assessment details
     */
    private static void viewAssessment(UserSession session, Long assessmentId, int userId) throws IOException {
        HttpGet get = new HttpGet(BASE_URL + "/portal/Assessment?id=" + assessmentId);
        addSessionCookie(get, session.sessionId);

        HttpResponse response = session.client.execute(get);
        EntityUtils.consume(response.getEntity());

        System.out.println("[User " + userId + "] Viewed assessment " + assessmentId);
    }

    /**
     * Generate report
     */
    private static boolean generateReport(UserSession session, Long assessmentId, int userId) throws IOException {
        HttpPost post = new HttpPost(BASE_URL + "/portal/SendToPR?id=" + assessmentId);
        addSessionCookie(post, session.sessionId);

        long start = System.currentTimeMillis();
        HttpResponse response = session.client.execute(post);
        long duration = System.currentTimeMillis() - start;

        boolean success = response.getStatusLine().getStatusCode() == 200;
        System.out.println(String.format(
            "[User %d] Report generation %s in %dms",
            userId,
            success ? "succeeded" : "failed",
            duration
        ));

        EntityUtils.consume(response.getEntity());
        return success;
    }

    /**
     * Check for lock updates (polling)
     */
    private static void checkForUpdates(UserSession session, Long assessmentId, int userId) throws IOException {
        HttpGet get = new HttpGet(BASE_URL + "/portal/GetStats?id=" + assessmentId);
        addSessionCookie(get, session.sessionId);

        HttpResponse response = session.client.execute(get);
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Helper methods
     */
    private static String extractSessionId(HttpResponse response) {
        // Extract JSESSIONID from Set-Cookie header
        String cookieHeader = response.getFirstHeader("Set-Cookie").getValue();
        if (cookieHeader != null && cookieHeader.contains("JSESSIONID")) {
            return cookieHeader.split(";")[0].split("=")[1];
        }
        return null;
    }

    private static void addSessionCookie(HttpGet request, String sessionId) {
        request.addHeader("Cookie", "JSESSIONID=" + sessionId);
    }

    private static void addSessionCookie(HttpPost request, String sessionId) {
        request.addHeader("Cookie", "JSESSIONID=" + sessionId);
    }

    /**
     * User session holder
     */
    static class UserSession {
        int userId;
        String sessionId;
        HttpClient client;

        UserSession(int userId, String sessionId, HttpClient client) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.client = client;
        }
    }
}
