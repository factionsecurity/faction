package com.fuse.loadtest;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.reporting.GenerateReport;

/**
 * Load test to simulate memory usage with concurrent report generation
 *
 * Usage:
 *   1. Run with monitoring: java -Xms2g -Xmx2g -XX:+PrintGCDetails MemoryLoadTest
 *   2. Watch memory consumption with VisualVM or JConsole
 *   3. Compare before/after optimization
 */
public class MemoryLoadTest {

    private static final int NUM_CONCURRENT_USERS = 16;
    private static final boolean TAKE_HEAP_DUMPS = true;
    private static final long ASSESSMENT_ID = 1L; // Change to valid assessment ID

    private static AtomicInteger completedReports = new AtomicInteger(0);
    private static AtomicInteger failedReports = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Faction Memory Load Test");
        System.out.println("===========================================");
        System.out.println("Concurrent Users: " + NUM_CONCURRENT_USERS);
        System.out.println("Assessment ID: " + ASSESSMENT_ID);
        System.out.println("===========================================\n");

        // Get baseline memory
        printMemoryUsage("BASELINE");

        try {
            // Test 1: Single report generation
            System.out.println("\n>>> TEST 1: Single Report Generation");
            testSingleReport();
            System.gc();
            Thread.sleep(2000);
            printMemoryUsage("AFTER SINGLE REPORT");

            // Test 2: Concurrent report generation (simulates production load)
            System.out.println("\n>>> TEST 2: " + NUM_CONCURRENT_USERS + " Concurrent Reports");

            if (TAKE_HEAP_DUMPS) {
                takeHeapDump("before-concurrent");
            }

            long startTime = System.currentTimeMillis();
            testConcurrentReports();
            long endTime = System.currentTimeMillis();

            System.out.println("\nCompleted: " + completedReports.get());
            System.out.println("Failed: " + failedReports.get());
            System.out.println("Duration: " + (endTime - startTime) + "ms");

            // Force GC and check memory
            System.out.println("\nForcing garbage collection...");
            System.gc();
            Thread.sleep(2000);

            printMemoryUsage("AFTER CONCURRENT REPORTS");

            if (TAKE_HEAP_DUMPS) {
                takeHeapDump("after-concurrent");
            }

            // Show retained memory
            printMemoryRetention();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n===========================================");
        System.out.println("Test Complete");
        System.out.println("===========================================");
    }

    /**
     * Test single report generation to establish baseline
     */
    private static void testSingleReport() {
        EntityManager em = null;
        try {
            em = HibHelper.getInstance().getEMF().createEntityManager();

            long before = getUsedMemoryMB();
            System.out.println("Memory before: " + before + " MB");

            GenerateReport genReport = new GenerateReport();
            String[] result = genReport.generateDocxReport(ASSESSMENT_ID, em, false);

            long after = getUsedMemoryMB();
            System.out.println("Memory after: " + after + " MB");
            System.out.println("Memory used by report: " + (after - before) + " MB");
            System.out.println("Report size: " + (result[0].length() / 1024 / 1024) + " MB (Base64)");

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Test concurrent report generation (simulates production scenario)
     */
    private static void testConcurrentReports() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CONCURRENT_USERS);
        CountDownLatch latch = new CountDownLatch(NUM_CONCURRENT_USERS);

        long startMemory = getUsedMemoryMB();
        System.out.println("Starting memory: " + startMemory + " MB");

        // Launch concurrent report generation
        for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    generateReportAsUser(userId);
                    completedReports.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("[User " + userId + "] Failed: " + e.getMessage());
                    failedReports.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Monitor memory while reports are generating
        Thread monitor = new Thread(() -> {
            try {
                while (latch.getCount() > 0) {
                    long currentMemory = getUsedMemoryMB();
                    int completed = completedReports.get();
                    System.out.println(String.format(
                        "[Monitor] Memory: %d MB | Completed: %d/%d | Active: %d",
                        currentMemory, completed, NUM_CONCURRENT_USERS,
                        NUM_CONCURRENT_USERS - completed - failedReports.get()
                    ));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        monitor.start();

        // Wait for all reports to complete
        latch.await();
        monitor.interrupt();

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long endMemory = getUsedMemoryMB();
        System.out.println("\nPeak memory: " + endMemory + " MB");
        System.out.println("Memory increase: " + (endMemory - startMemory) + " MB");
        System.out.println("Avg per report: " + ((endMemory - startMemory) / NUM_CONCURRENT_USERS) + " MB");
    }

    /**
     * Simulate report generation by a single user
     */
    private static void generateReportAsUser(int userId) throws Exception {
        EntityManager em = null;
        try {
            System.out.println("[User " + userId + "] Starting report generation...");
            long userStartMem = getUsedMemoryMB();
            long startTime = System.currentTimeMillis();

            em = HibHelper.getInstance().getEMF().createEntityManager();
            GenerateReport genReport = new GenerateReport();
            String[] result = genReport.generateDocxReport(ASSESSMENT_ID, em, false);

            long endTime = System.currentTimeMillis();
            long userEndMem = getUsedMemoryMB();

            System.out.println(String.format(
                "[User %d] Completed in %dms | Memory delta: %d MB | Report size: %d MB",
                userId, (endTime - startTime), (userEndMem - userStartMem),
                result[0].length() / 1024 / 1024
            ));

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Print current memory usage
     */
    private static void printMemoryUsage(String label) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        System.out.println("\n--- " + label + " ---");
        System.out.println("Heap Memory:");
        System.out.println("  Used: " + (heapUsage.getUsed() / 1024 / 1024) + " MB");
        System.out.println("  Committed: " + (heapUsage.getCommitted() / 1024 / 1024) + " MB");
        System.out.println("  Max: " + (heapUsage.getMax() / 1024 / 1024) + " MB");
        System.out.println("Non-Heap Memory:");
        System.out.println("  Used: " + (nonHeapUsage.getUsed() / 1024 / 1024) + " MB");
        System.out.println("  Committed: " + (nonHeapUsage.getCommitted() / 1024 / 1024) + " MB");
    }

    /**
     * Get current used heap memory in MB
     */
    private static long getUsedMemoryMB() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getUsed() / 1024 / 1024;
    }

    /**
     * Check for memory retention issues
     */
    private static void printMemoryRetention() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        System.out.println("\n--- Memory Retention Analysis ---");
        System.out.println("Total: " + totalMemory + " MB");
        System.out.println("Used: " + usedMemory + " MB");
        System.out.println("Free: " + freeMemory + " MB");

        if (usedMemory > (totalMemory * 0.8)) {
            System.out.println("WARNING: High memory retention detected!");
            System.out.println("Possible memory leak - check for:");
            System.out.println("  - Unclosed EntityManagers");
            System.out.println("  - Large objects in session");
            System.out.println("  - Base64 encoded reports in memory");
        }
    }

    /**
     * Take a heap dump for analysis
     */
    private static void takeHeapDump(String label) {
        try {
            String filename = "heap-dump-" + label + "-" + System.currentTimeMillis() + ".hprof";

            // Use HotSpot diagnostic MBean
            com.sun.management.HotSpotDiagnosticMXBean mxBean =
                ManagementFactory.newPlatformMXBeanProxy(
                    ManagementFactory.getPlatformMBeanServer(),
                    "com.sun.management:type=HotSpotDiagnostic",
                    com.sun.management.HotSpotDiagnosticMXBean.class
                );

            mxBean.dumpHeap(filename, true);
            System.out.println("Heap dump saved: " + filename);
            System.out.println("Analyze with: jvisualvm --openfile " + filename);

        } catch (Exception e) {
            System.err.println("Could not take heap dump: " + e.getMessage());
        }
    }
}
