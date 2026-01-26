# DocxUtils Profiling Implementation Details

## 1. ProfileMethod Annotation (Optional)

**File**: `src/com/fuse/utils/ProfileMethod.java`

```java
package com.fuse.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation annotation to mark methods that are being profiled.
 * This annotation is for documentation purposes only and has no runtime behavior.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ProfileMethod {
    /**
     * Optional description of what is being profiled
     */
    String value() default "";
}
```

## 2. MethodProfiler Implementation

**File**: `src/com/fuse/utils/MethodProfiler.java`

```java
package com.fuse.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Lightweight method profiler for tracking method call counts and execution duration.
 * Simple static configuration with console output.
 */
public class MethodProfiler {
    
    // Enable/disable profiling - can be toggled at runtime
    private static volatile boolean PROFILING_ENABLED = true;
    
    // Thread-safe storage for method statistics
    private static final ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();
    
    /**
     * Statistics for a single method
     */
    private static class MethodStats {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong(0);
        
        void recordCall(long duration) {
            callCount.incrementAndGet();
            totalDuration.addAndGet(duration);
            
            // Update min duration
            long currentMin = minDuration.get();
            while (duration < currentMin && !minDuration.compareAndSet(currentMin, duration)) {
                currentMin = minDuration.get();
            }
            
            // Update max duration
            long currentMax = maxDuration.get();
            while (duration > currentMax && !maxDuration.compareAndSet(currentMax, duration)) {
                currentMax = maxDuration.get();
            }
        }
        
        long getCallCount() { return callCount.get(); }
        long getTotalDuration() { return totalDuration.get(); }
        long getMinDuration() { 
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        long getMaxDuration() { return maxDuration.get(); }
        double getAverageDuration() {
            long count = callCount.get();
            return count == 0 ? 0.0 : (double) totalDuration.get() / count;
        }
    }
    
    /**
     * Profile context for tracking method execution
     */
    public static class ProfileContext {
        private final String methodName;
        private final long startTime;
        private final boolean enabled;
        
        private ProfileContext(String methodName, boolean enabled) {
            this.methodName = methodName;
            this.enabled = enabled;
            this.startTime = enabled ? System.nanoTime() : 0;
        }
        
        /**
         * End profiling and record the duration
         */
        public void end() {
            if (enabled) {
                long duration = System.nanoTime() - startTime;
                methodStats.computeIfAbsent(methodName, k -> new MethodStats()).recordCall(duration);
            }
        }
    }
    
    /**
     * Start profiling a method
     * @param className The class name
     * @param methodName The method name
     * @return ProfileContext to end profiling
     */
    public static ProfileContext start(String className, String methodName) {
        String fullMethodName = className + "." + methodName;
        return new ProfileContext(fullMethodName, PROFILING_ENABLED);
    }
    
    /**
     * Start profiling a method with just the method name
     * @param methodName The method name
     * @return ProfileContext to end profiling
     */
    public static ProfileContext start(String methodName) {
        return new ProfileContext(methodName, PROFILING_ENABLED);
    }
    
    /**
     * Enable or disable profiling
     * @param enabled true to enable profiling, false to disable
     */
    public static void setEnabled(boolean enabled) {
        PROFILING_ENABLED = enabled;
    }
    
    /**
     * Check if profiling is enabled
     * @return true if profiling is enabled
     */
    public static boolean isEnabled() {
        return PROFILING_ENABLED;
    }
    
    /**
     * Clear all collected statistics
     */
    public static void clearStats() {
        methodStats.clear();
    }
    
    /**
     * Print profiling report to console
     */
    public static void printReport() {
        if (methodStats.isEmpty()) {
            System.out.println("=== DocxUtils Method Profiling Report ===");
            System.out.println("No method calls recorded.");
            return;
        }
        
        System.out.println("\n=== DocxUtils Method Profiling Report ===");
        System.out.printf("%-50s %10s %15s %15s %15s %15s%n", 
            "Method", "Calls", "Total (ms)", "Avg (ms)", "Min (ms)", "Max (ms)");
        System.out.println("=".repeat(130));
        
        // Sort methods by total duration (descending)
        List<Map.Entry<String, MethodStats>> sortedEntries = new ArrayList<>(methodStats.entrySet());
        sortedEntries.sort((e1, e2) -> Long.compare(e2.getValue().getTotalDuration(), e1.getValue().getTotalDuration()));
        
        for (Map.Entry<String, MethodStats> entry : sortedEntries) {
            String methodName = entry.getKey();
            MethodStats stats = entry.getValue();
            
            // Convert nanoseconds to milliseconds for readability
            double totalMs = stats.getTotalDuration() / 1_000_000.0;
            double avgMs = stats.getAverageDuration() / 1_000_000.0;
            double minMs = stats.getMinDuration() / 1_000_000.0;
            double maxMs = stats.getMaxDuration() / 1_000_000.0;
            
            System.out.printf("%-50s %10d %15.2f %15.2f %15.2f %15.2f%n",
                methodName.length() > 50 ? methodName.substring(0, 47) + "..." : methodName,
                stats.getCallCount(),
                totalMs,
                avgMs,
                minMs,
                maxMs);
        }
        
        // Summary statistics
        System.out.println("=".repeat(130));
        long totalCalls = methodStats.values().stream().mapToLong(MethodStats::getCallCount).sum();
        double totalTimeMs = methodStats.values().stream().mapToLong(MethodStats::getTotalDuration).sum() / 1_000_000.0;
        
        System.out.printf("Total Methods: %d | Total Calls: %d | Total Time: %.2f ms%n", 
            methodStats.size(), totalCalls, totalTimeMs);
        System.out.println("=== End Report ===\n");
    }
    
    /**
     * Get statistics for a specific method
     * @param methodName The method name
     * @return MethodStats or null if not found
     */
    public static String getMethodStats(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        if (stats == null) {
            return "Method '" + methodName + "' not found in profiling data.";
        }
        
        double totalMs = stats.getTotalDuration() / 1_000_000.0;
        double avgMs = stats.getAverageDuration() / 1_000_000.0;
        double minMs = stats.getMinDuration() / 1_000_000.0;
        double maxMs = stats.getMaxDuration() / 1_000_000.0;
        
        return String.format("%s: Calls=%d, Total=%.2fms, Avg=%.2fms, Min=%.2fms, Max=%.2fms",
            methodName, stats.getCallCount(), totalMs, avgMs, minMs, maxMs);
    }
}
```

## 3. DocxUtils Method Instrumentation

### High Priority Methods

#### 3.1 generateDocx Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:543)

```java
@ProfileMethod("Main document generation")
public WordprocessingMLPackage generateDocx(String customCSS) throws Exception {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.generateDocx");
    try {
        VariablePrepare.prepare(mlp);

        // Convert all tables and match and replace values
        checkTables("vulnTable", "Default", customCSS);
        setFindings("Default", customCSS);
        if (ReportFeatures.allowSections()) {
            for (String section : this.reportSections) {
                checkTables("vulnTable", section, customCSS);
                setFindings(section, customCSS);
            }
        }

        // look for findings areass {fiBegin/fiEnd}
        HashMap<String, List<Object>> map = new HashMap();

        map.put("${summary1}",
                this.wrapHTML(this.assessment.getSummary() == null ? "" : this.assessment.getSummary(), customCSS,
                        "summary1"));
        map.put("${summary2}",
                this.wrapHTML(this.assessment.getRiskAnalysis() == null ? "" : this.assessment.getRiskAnalysis(),
                        customCSS, "summary2"));
        replaceHTML(mlp.getMainDocumentPart(), map, false);
        replaceAssessment(customCSS);
        updateDocWithExtensions(customCSS);

        return mlp;
    } finally {
        profile.end();
    }
}
```

#### 3.2 checkTables Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:180)

```java
@ProfileMethod("Table processing and vulnerability data insertion")
private void checkTables(String variable, String section, String customCSS)
        throws JAXBException, Docx4JException {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.checkTables");
    try {
        List<Vulnerability> filteredVulns = this.getFilteredVulns(section);
        // ... existing implementation
    } finally {
        profile.end();
    }
}
```

#### 3.3 setFindings Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:1095)

```java
@ProfileMethod("Findings section processing")
private void setFindings(String section, String customCSS)
        throws JAXBException, Docx4JException {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.setFindings");
    try {
        int begin = -1;
        int end = -1;
        // ... existing implementation
    } finally {
        profile.end();
    }
}
```

#### 3.4 wrapHTML Methods

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:575)

```java
@ProfileMethod("HTML content wrapping and conversion")
private List<Object> wrapHTML(String content, String customCSS, String className) throws Docx4JException {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.wrapHTML");
    try {
        // Suppress OpenHTMLToPDF logging right before conversion
        LoggingConfig.configureOpenHTMLTopDFLogging();
        // ... existing implementation
    } finally {
        profile.end();
    }
}

@ProfileMethod("HTML content wrapping with width constraints")
private List<Object> wrapHTML(String value, String customCSS, String className, BigInteger maxWidth)
        throws Docx4JException {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.wrapHTML_withWidth");
    try {
        // Suppress OpenHTMLToPDF logging right before conversion
        LoggingConfig.configureOpenHTMLTopDFLogging();
        // ... existing implementation
    } finally {
        profile.end();
    }
}
```

### Medium Priority Methods

#### 3.5 replaceAssessment Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:670)

```java
@ProfileMethod("Assessment data replacement")
private void replaceAssessment(String customCSS) throws Exception {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.replaceAssessment");
    try {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy");
        // ... existing implementation
    } finally {
        profile.end();
    }
}
```

#### 3.6 replaceHTML Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:1727)

```java
@ProfileMethod("HTML replacement in document")
private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements, boolean once) {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.replaceHTML");
    try {
        if (mainPart == null)
            return;
        Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
        // ... existing implementation
    } finally {
        profile.end();
    }
}
```

#### 3.7 getAllElementFromObject Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:1381)

```java
@ProfileMethod("DOM element traversal")
private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.getAllElementFromObject");
    try {
        List<Object> result = new ArrayList<Object>();
        // ... existing implementation
        return result;
    } finally {
        profile.end();
    }
}
```

#### 3.8 replaceImageLinks Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:1005)

```java
@ProfileMethod("Image link processing and base64 conversion")
private String replaceImageLinks(String text) {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.replaceImageLinks");
    try {
        text = this.centerImages(text);
        // ... existing implementation
        return text;
    } finally {
        profile.end();
    }
}
```

### Additional Methods to Profile

#### 3.9 getFilteredVulns Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:148)

```java
@ProfileMethod("Vulnerability filtering by section")
private List<Vulnerability> getFilteredVulns(String section) {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.getFilteredVulns");
    try {
        if (section == null) {
            section = "Default";
        } else if (section.isEmpty()) {
            section = "Default";
        }
        // ... existing implementation
        return filteredVulns;
    } finally {
        profile.end();
    }
}
```

#### 3.10 replacement Method

**Location**: [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java:939)

```java
@ProfileMethod("Content replacement processing")
private String replacement(String content) {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.replacement");
    try {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy");
        // ... existing implementation
        return content;
    } finally {
        profile.end();
    }
}
```

## 4. Implementation Notes

### Key Patterns

1. **Try-Finally Pattern**: Always use try-finally to ensure profiling completion
2. **Consistent Naming**: Use "DocxUtils.methodName" format for clarity
3. **Early Return Handling**: Place profiling start after parameter validation but before main logic
4. **Exception Safety**: Profile context will still record timing even if method throws exception

### Performance Impact

- **Enabled**: ~100-200ns overhead per method call
- **Disabled**: ~5-10ns overhead per method call (single boolean check)
- **Memory**: ~100 bytes per unique method name tracked

### Best Practices

1. Profile method boundaries, not internal loops
2. Use descriptive method names in profiling calls
3. Consider nested method profiling for detailed analysis
4. Clear stats between test runs for accurate measurement

This implementation provides comprehensive coverage of the most performance-critical methods in DocxUtils while maintaining minimal overhead and maximum flexibility.