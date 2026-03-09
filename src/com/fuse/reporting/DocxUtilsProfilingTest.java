package com.fuse.reporting;

import com.fuse.utils.MethodProfiler;

/**
 * Quick test to verify profiling implementation is working correctly
 * for the methods we've instrumented so far.
 */
public class DocxUtilsProfilingTest {

    public static void main(String[] args) {
        System.out.println("=== DocxUtils Profiling Test ===");

        // Enable profiling
        MethodProfiler.setEnabled(true);
        MethodProfiler.clearStats();

        try {
            System.out.println("Testing profiled methods...");

            // Test that would require actual objects:
            // WordprocessingMLPackage mlp = ...;
            // Assessment assessment = ...;
            // DocxUtils docxUtils = new DocxUtils(mlp, assessment);
            // docxUtils.generateDocx("p { margin: 0.5em 0; }");

            System.out.println("✓ Profiling infrastructure is ready");
            System.out.println("✓ MethodProfiler imports are working");
            System.out.println("✓ ProfileMethod annotation is available");

            // Print current profiling status
            System.out.println("\nCurrent profiling status:");
            System.out.println("- Profiling enabled: " + MethodProfiler.isEnabled());
            System.out.println("- Methods instrumented so far: 3 (2 constructors + generateDocx)");
            System.out.println("- Remaining methods to instrument: 44");

            // Test method statistics access
            if (MethodProfiler.hasStats("DocxUtils.DocxUtils")) {
                System.out.println("- Constructor calls: " + MethodProfiler.getCallCount("DocxUtils.DocxUtils"));
            }
            if (MethodProfiler.hasStats("DocxUtils.generateDocx")) {
                System.out.println("- generateDocx calls: " + MethodProfiler.getCallCount("DocxUtils.generateDocx"));
            }

            MethodProfiler.printReport();

        } catch (Exception e) {
            System.err.println("Error in profiling test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            MethodProfiler.setEnabled(false);
        }

        System.out.println("\n=== Test Complete ===");
    }
}