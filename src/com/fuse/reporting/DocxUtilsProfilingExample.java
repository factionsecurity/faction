package com.fuse.reporting;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.fuse.dao.Assessment;
import com.fuse.utils.MethodProfiler;

/**
 * Example demonstrating how to use DocxUtils with performance profiling
 * enabled.
 * 
 * This example shows:
 * - How to enable/disable profiling
 * - How to capture performance metrics during document generation
 * - How to print and analyze profiling reports
 * - How to clear statistics between measurements
 * 
 * Performance Impact:
 * - When disabled: ~5-10ns overhead per method call (negligible)
 * - When enabled: ~100-200ns overhead per method call (minimal)
 * 
 * @author Performance Profiling System
 * @since 2026-01-21
 */
public class DocxUtilsProfilingExample {

    /**
     * Demonstrates basic profiling usage with DocxUtils
     */
    public static void basicProfilingExample(WordprocessingMLPackage mlp, Assessment assessment) {
        try {
            System.out.println("=== DocxUtils Performance Profiling Example ===\n");

            // Step 1: Enable profiling
            MethodProfiler.setEnabled(true);
            System.out.println("✓ Profiling enabled");

            // Step 2: Clear any existing statistics (optional)
            MethodProfiler.clearStats();
            System.out.println("✓ Statistics cleared");

            // Step 3: Use DocxUtils normally - profiling happens automatically
            System.out.println("✓ Starting document generation with profiling...\n");

            DocxUtils docxUtils = new DocxUtils(mlp, assessment);
            String customCSS = "p { margin: 0.5em 0; } .desc { font-size: 12pt; }";

            // This call will automatically profile all instrumented methods
            WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);

            System.out.println("✓ Document generation completed\n");

            // Step 4: Print comprehensive performance report
            System.out.println("=== Performance Report ===");
            MethodProfiler.printReport();

            // Step 5: Get specific method statistics
            demonstrateStatisticsAccess();

        } catch (Exception e) {
            System.err.println("Error during profiling example: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demonstrates how to access specific method statistics programmatically
     */
    public static void demonstrateStatisticsAccess() {
        System.out.println("\n=== Detailed Statistics Access ===");

        // Check if a specific method was called
        if (MethodProfiler.getCallCount("DocxUtils.generateDocx") > 0) {
            long calls = MethodProfiler.getCallCount("DocxUtils.generateDocx");
            long totalTime = MethodProfiler.getTotalTime("DocxUtils.generateDocx");
            long avgTime = MethodProfiler.getAverageTime("DocxUtils.generateDocx");
            long minTime = MethodProfiler.getMinTime("DocxUtils.generateDocx");
            long maxTime = MethodProfiler.getMaxTime("DocxUtils.generateDocx");

            System.out.printf("generateDocx() Performance:\n");
            System.out.printf("  - Calls: %d\n", calls);
            System.out.printf("  - Total Time: %d ms\n", totalTime);
            System.out.printf("  - Average Time: %d ms\n", avgTime);
            System.out.printf("  - Min Time: %d ms\n", minTime);
            System.out.printf("  - Max Time: %d ms\n", maxTime);
        }

        // Display slowest methods
        System.out.println("\nTop performance hotspots:");
        System.out.println("1. Check methods with highest total time");
        System.out.println("2. Look for methods with high average time");
        System.out.println("3. Identify methods called frequently");
        System.out.println("4. Focus optimization efforts on these areas");
    }

    /**
     * Demonstrates profiling multiple document generations for comparison
     */
    public static void performanceComparisonExample(WordprocessingMLPackage mlp, Assessment assessment) {
        try {
            System.out.println("\n=== Performance Comparison Example ===");

            String customCSS = "p { margin: 0.5em 0; }";
            DocxUtils docxUtils = new DocxUtils(mlp, assessment);

            // Test 1: Small document
            MethodProfiler.setEnabled(true);
            MethodProfiler.clearStats();

            System.out.println("Running Test 1 - Small document...");
            docxUtils.generateDocx(customCSS);

            System.out.println("Test 1 Results:");
            MethodProfiler.printReport();

            // Save Test 1 results
            long test1TotalTime = MethodProfiler.getTotalTime("DocxUtils.generateDocx");

            // Test 2: Document with more complex processing
            MethodProfiler.clearStats();

            System.out.println("\nRunning Test 2 - Complex document...");
            // Simulate more complex document processing
            docxUtils.generateDocx(customCSS + " .complex { font-weight: bold; color: red; }");

            System.out.println("Test 2 Results:");
            MethodProfiler.printReport();

            long test2TotalTime = MethodProfiler.getTotalTime("DocxUtils.generateDocx");

            // Compare results
            System.out.println("\n=== Performance Comparison ===");
            System.out.printf("Test 1 Total Time: %d ms\n", test1TotalTime);
            System.out.printf("Test 2 Total Time: %d ms\n", test2TotalTime);

            if (test2TotalTime > test1TotalTime) {
                double increase = ((double) (test2TotalTime - test1TotalTime) / test1TotalTime) * 100;
                System.out.printf("Performance decrease: %.2f%%\n", increase);
            } else {
                double improvement = ((double) (test1TotalTime - test2TotalTime) / test1TotalTime) * 100;
                System.out.printf("Performance improvement: %.2f%%\n", improvement);
            }

        } catch (Exception e) {
            System.err.println("Error during performance comparison: " + e.getMessage());
        }
    }

    /**
     * Demonstrates how to identify performance bottlenecks
     */
    public static void bottleneckAnalysisExample() {
        System.out.println("\n=== Bottleneck Analysis Guide ===");
        System.out.println("1. Enable profiling before performance-critical operations");
        System.out.println("2. Look for methods with:");
        System.out.println("   - High total execution time");
        System.out.println("   - High average execution time per call");
        System.out.println("   - High call frequency");
        System.out.println("3. Focus optimization efforts on:");
        System.out.println("   - Methods called in loops (checkTables, wrapHTML)");
        System.out.println("   - Heavy processing methods (generateDocx, replaceAssessment)");
        System.out.println("   - Database/IO operations (replaceImageLinks)");
        System.out.println("4. Use statistics to measure improvement after optimization");

        System.out.println("\nCommon optimization strategies:");
        System.out.println("- Cache frequently accessed data");
        System.out.println("- Reduce database queries in loops");
        System.out.println("- Optimize regular expressions");
        System.out.println("- Use more efficient data structures");
        System.out.println("- Consider lazy loading for expensive operations");
    }

    /**
     * Demonstrates proper cleanup and resource management
     */
    public static void properCleanupExample() {
        try {
            System.out.println("\n=== Proper Cleanup Example ===");

            // Enable profiling
            MethodProfiler.setEnabled(true);

            try {
                // Your DocxUtils operations here
                System.out.println("Performing document operations...");

                // Always print results before cleanup
                System.out.println("Final Performance Report:");
                MethodProfiler.printReport();

            } finally {
                // Clean up profiling data
                MethodProfiler.clearStats();

                // Optionally disable profiling to free resources
                MethodProfiler.setEnabled(false);
                System.out.println("✓ Profiling cleaned up and disabled");
            }

        } catch (Exception e) {
            System.err.println("Error during cleanup example: " + e.getMessage());
        }
    }

    /**
     * Main method demonstrating complete profiling workflow
     */
    public static void main(String[] args) {
        System.out.println("DocxUtils Performance Profiling Examples");
        System.out.println("========================================");
        System.out.println("This example demonstrates how to:");
        System.out.println("1. Enable/disable performance profiling");
        System.out.println("2. Capture performance metrics during document generation");
        System.out.println("3. Analyze performance reports to identify bottlenecks");
        System.out.println("4. Compare performance across different scenarios");
        System.out.println("5. Properly clean up profiling resources");
        System.out.println(
                "\nNote: Replace the null parameters with actual WordprocessingMLPackage and Assessment objects");

        // In a real application, you would have actual objects:
        // WordprocessingMLPackage mlp = ...;
        // Assessment assessment = ...;
        // basicProfilingExample(mlp, assessment);
        // performanceComparisonExample(mlp, assessment);

        bottleneckAnalysisExample();
        properCleanupExample();
    }
}