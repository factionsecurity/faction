# DocxUtils Profiling Usage Examples and Testing

## Usage Examples

### Basic Usage

```java
// Example 1: Basic document generation with profiling
public class DocxUtilsExample {
    public void generateReportWithProfiling() {
        // Enable profiling (it's enabled by default)
        MethodProfiler.setEnabled(true);
        
        try {
            // Create DocxUtils instance
            DocxUtils docxUtils = new DocxUtils(mlp, assessment);
            
            // Generate document - profiling happens automatically
            WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);
            
            // Print performance report
            MethodProfiler.printReport();
            
        } finally {
            // Clear stats for next run
            MethodProfiler.clearStats();
        }
    }
}
```

### Development Environment Usage

```java
// Example 2: Conditional profiling based on system property
public class DocxUtilsService {
    private static final boolean PROFILING_ENABLED = 
        Boolean.parseBoolean(System.getProperty("docx.profiling.enabled", "false"));
    
    public WordprocessingMLPackage generateDocument(Assessment assessment, String customCSS) {
        // Enable profiling only in development/debug mode
        MethodProfiler.setEnabled(PROFILING_ENABLED);
        
        try {
            DocxUtils docxUtils = new DocxUtils(mlp, assessment);
            WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);
            
            if (PROFILING_ENABLED) {
                System.out.println("=== Performance Report for Assessment: " + assessment.getId() + " ===");
                MethodProfiler.printReport();
            }
            
            return result;
        } finally {
            if (PROFILING_ENABLED) {
                MethodProfiler.clearStats();
            }
        }
    }
}
```

### Batch Processing with Profiling

```java
// Example 3: Profiling across multiple document generations
public class BatchDocumentProcessor {
    public void processBatch(List<Assessment> assessments, String customCSS) {
        MethodProfiler.setEnabled(true);
        
        System.out.println("Processing " + assessments.size() + " assessments...");
        
        for (int i = 0; i < assessments.size(); i++) {
            Assessment assessment = assessments.get(i);
            
            try {
                DocxUtils docxUtils = new DocxUtils(mlp, assessment);
                WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);
                
                // Save document logic here...
                
                System.out.println("Completed assessment " + (i + 1) + "/" + assessments.size() + 
                    " (ID: " + assessment.getId() + ")");
                
            } catch (Exception e) {
                System.err.println("Error processing assessment " + assessment.getId() + ": " + e.getMessage());
            }
        }
        
        // Print aggregate performance report
        System.out.println("\n=== Batch Processing Performance Report ===");
        MethodProfiler.printReport();
        
        // Optionally save stats before clearing
        // saveProfilingStats();
        
        MethodProfiler.clearStats();
    }
}
```

### Selective Method Profiling

```java
// Example 4: Checking specific method performance
public class PerformanceAnalyzer {
    public void analyzeSpecificMethods() {
        MethodProfiler.setEnabled(true);
        
        // Run some operations...
        DocxUtils docxUtils = new DocxUtils(mlp, assessment);
        docxUtils.generateDocx(customCSS);
        
        // Check specific method performance
        System.out.println(MethodProfiler.getMethodStats("DocxUtils.generateDocx"));
        System.out.println(MethodProfiler.getMethodStats("DocxUtils.checkTables"));
        System.out.println(MethodProfiler.getMethodStats("DocxUtils.wrapHTML"));
        
        MethodProfiler.clearStats();
    }
}
```

## Testing Scenarios

### Test 1: Unit Test for Profiler Functionality

```java
// File: test/com/fuse/utils/MethodProfilerTest.java
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class MethodProfilerTest {
    
    @Before
    public void setup() {
        MethodProfiler.clearStats();
        MethodProfiler.setEnabled(true);
    }
    
    @After
    public void cleanup() {
        MethodProfiler.clearStats();
    }
    
    @Test
    public void testBasicProfiling() {
        // Test basic profiling functionality
        MethodProfiler.ProfileContext context = MethodProfiler.start("test.method");
        
        // Simulate some work
        try {
            Thread.sleep(10); // 10ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        context.end();
        
        // Verify profiling recorded the call
        String stats = MethodProfiler.getMethodStats("test.method");
        assertTrue("Stats should contain method name", stats.contains("test.method"));
        assertTrue("Stats should show 1 call", stats.contains("Calls=1"));
    }
    
    @Test
    public void testProfilingDisabled() {
        MethodProfiler.setEnabled(false);
        
        MethodProfiler.ProfileContext context = MethodProfiler.start("disabled.method");
        context.end();
        
        // Should not record when disabled
        String stats = MethodProfiler.getMethodStats("disabled.method");
        assertTrue("Method should not be found when profiling disabled", 
            stats.contains("not found"));
    }
    
    @Test
    public void testMultipleCalls() {
        // Test multiple calls to same method
        for (int i = 0; i < 5; i++) {
            MethodProfiler.ProfileContext context = MethodProfiler.start("repeated.method");
            // Simulate variable work duration
            try {
                Thread.sleep(i + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            context.end();
        }
        
        String stats = MethodProfiler.getMethodStats("repeated.method");
        assertTrue("Should show 5 calls", stats.contains("Calls=5"));
    }
}
```

### Test 2: Integration Test with Sample DocxUtils

```java
// File: test/com/fuse/reporting/DocxUtilsProfilingTest.java
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.fuse.utils.MethodProfiler;

public class DocxUtilsProfilingTest {
    
    private Assessment sampleAssessment;
    private WordprocessingMLPackage mlp;
    private String customCSS = "body { font-family: Arial; }";
    
    @Before
    public void setup() {
        // Setup sample data
        sampleAssessment = createSampleAssessment();
        mlp = createSampleMLPackage();
        
        MethodProfiler.clearStats();
        MethodProfiler.setEnabled(true);
    }
    
    @After
    public void cleanup() {
        MethodProfiler.clearStats();
    }
    
    @Test
    public void testDocxGenerationProfiling() throws Exception {
        DocxUtils docxUtils = new DocxUtils(mlp, sampleAssessment);
        
        // Generate document with profiling
        WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);
        
        // Print report for manual verification
        System.out.println("=== Integration Test Profiling Report ===");
        MethodProfiler.printReport();
        
        // Verify key methods were profiled
        assertMethodWasProfiled("DocxUtils.generateDocx");
        assertMethodWasProfiled("DocxUtils.checkTables");
        assertMethodWasProfiled("DocxUtils.setFindings");
        
        assertNotNull("Generated document should not be null", result);
    }
    
    @Test
    public void testLargeDocumentProfiling() throws Exception {
        // Create assessment with many vulnerabilities
        Assessment largeAssessment = createLargeAssessment(50); // 50 vulnerabilities
        
        DocxUtils docxUtils = new DocxUtils(mlp, largeAssessment);
        WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);
        
        System.out.println("=== Large Document Profiling Report ===");
        MethodProfiler.printReport();
        
        // Verify performance characteristics
        String generateStats = MethodProfiler.getMethodStats("DocxUtils.generateDocx");
        String checkTablesStats = MethodProfiler.getMethodStats("DocxUtils.checkTables");
        
        System.out.println("Generate Stats: " + generateStats);
        System.out.println("CheckTables Stats: " + checkTablesStats);
        
        assertNotNull("Generated large document should not be null", result);
    }
    
    private void assertMethodWasProfiled(String methodName) {
        String stats = MethodProfiler.getMethodStats(methodName);
        assertFalse("Method " + methodName + " should be profiled", 
            stats.contains("not found"));
    }
    
    private Assessment createSampleAssessment() {
        // Create sample assessment with test data
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setName("Test Assessment");
        
        // Add sample vulnerabilities
        List<Vulnerability> vulns = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Vulnerability vuln = new Vulnerability();
            vuln.setName("Test Vulnerability " + i);
            vuln.setOverall((long)(i % 4)); // Mix of severity levels
            vulns.add(vuln);
        }
        assessment.setVulns(vulns);
        
        return assessment;
    }
    
    private Assessment createLargeAssessment(int vulnCount) {
        Assessment assessment = createSampleAssessment();
        
        List<Vulnerability> vulns = new ArrayList<>();
        for (int i = 0; i < vulnCount; i++) {
            Vulnerability vuln = new Vulnerability();
            vuln.setName("Large Test Vulnerability " + i);
            vuln.setOverall((long)(i % 4));
            vuln.setDescription("This is a detailed description for vulnerability " + i);
            vuln.setRecommendation("This is a detailed recommendation for vulnerability " + i);
            vulns.add(vuln);
        }
        assessment.setVulns(vulns);
        
        return assessment;
    }
    
    private WordprocessingMLPackage createSampleMLPackage() {
        // Create sample WordprocessingMLPackage for testing
        try {
            return WordprocessingMLPackage.createPackage();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sample ML package", e);
        }
    }
}
```

### Test 3: Performance Benchmark Test

```java
// File: test/com/fuse/reporting/DocxUtilsPerformanceTest.java
import org.junit.Test;
import org.junit.Before;
import com.fuse.utils.MethodProfiler;

public class DocxUtilsPerformanceTest {
    
    @Before
    public void setup() {
        MethodProfiler.clearStats();
    }
    
    @Test
    public void benchmarkProfilingOverhead() throws Exception {
        Assessment assessment = createSampleAssessment();
        WordprocessingMLPackage mlp = createSampleMLPackage();
        String customCSS = "body { font-family: Arial; }";
        
        // Test without profiling
        MethodProfiler.setEnabled(false);
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 10; i++) {
            DocxUtils docxUtils = new DocxUtils(mlp, assessment);
            docxUtils.generateDocx(customCSS);
        }
        
        long noprofileTime = System.nanoTime() - startTime;
        
        // Test with profiling
        MethodProfiler.setEnabled(true);
        startTime = System.nanoTime();
        
        for (int i = 0; i < 10; i++) {
            DocxUtils docxUtils = new DocxUtils(mlp, assessment);
            docxUtils.generateDocx(customCSS);
        }
        
        long profileTime = System.nanoTime() - startTime;
        
        double overhead = ((double)(profileTime - noprofileTime) / noprofileTime) * 100;
        
        System.out.println("=== Profiling Overhead Analysis ===");
        System.out.println("Without profiling: " + (noprofileTime / 1_000_000.0) + " ms");
        System.out.println("With profiling: " + (profileTime / 1_000_000.0) + " ms");
        System.out.println("Overhead: " + String.format("%.2f%%", overhead));
        
        MethodProfiler.printReport();
        
        // Overhead should be minimal (typically < 5%)
        assertTrue("Profiling overhead should be reasonable", overhead < 10.0);
    }
}
```

## Expected Output Examples

### Sample Console Output

```
=== DocxUtils Method Profiling Report ===
Method                                     Calls      Total (ms)        Avg (ms)        Min (ms)        Max (ms)
==============================================================================================================================
DocxUtils.generateDocx                         1          1,234.56      1,234.56         1,234.56        1,234.56
DocxUtils.checkTables                          2            456.78        228.39            123.45          333.33
DocxUtils.wrapHTML                            25            234.12          9.36              1.23           45.67
DocxUtils.setFindings                          2            189.45         94.73             67.89          121.56
DocxUtils.replaceAssessment                    1             89.23         89.23             89.23           89.23
DocxUtils.replaceHTML                          8             67.89          8.49              2.34           23.45
DocxUtils.getAllElementFromObject             45             45.67          1.01              0.12            8.90
DocxUtils.replaceImageLinks                    3             34.56         11.52              5.67           18.90
DocxUtils.getFilteredVulns                     4             23.45          5.86              3.45            9.87
DocxUtils.replacement                         12             12.34          1.03              0.45            2.34
==============================================================================================================================
Total Methods: 10 | Total Calls: 103 | Total Time: 2,387.25 ms
=== End Report ===
```

## JVM Arguments for Testing

To enable profiling via system properties:

```bash
# Enable profiling
java -Ddocx.profiling.enabled=true -jar your-application.jar

# Disable profiling for production
java -Ddocx.profiling.enabled=false -jar your-application.jar
```

## Best Practices for Testing

1. **Isolated Testing**: Clear profiling stats between test runs
2. **Realistic Data**: Use assessments with realistic vulnerability counts
3. **Multiple Runs**: Average results over multiple runs for accuracy
4. **Memory Testing**: Monitor memory usage during profiling
5. **Thread Safety**: Test concurrent document generation
6. **Edge Cases**: Test with empty assessments, large assessments, malformed data

This comprehensive testing approach ensures the profiling solution is robust, accurate, and provides valuable performance insights for DocxUtils optimization.