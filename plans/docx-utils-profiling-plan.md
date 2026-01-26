# DocxUtils Performance Profiling Implementation Plan

## Overview
This plan outlines the implementation of a lightweight performance monitoring solution for the DocxUtils class to measure method call counts and execution duration locally.

## Key Performance-Critical Methods Identified

Based on analysis of [`DocxUtils.java`](../src/com/fuse/reporting/DocxUtils.java), the following methods are identified as performance-critical:

### High Priority Methods (Main Entry Points)
- `generateDocx(String customCSS)` - Main document generation method
- `checkTables(String variable, String section, String customCSS)` - Table processing
- `setFindings(String section, String customCSS)` - Findings processing

### Medium Priority Methods (Heavy Processing)
- `wrapHTML(String content, String customCSS, String className)` - HTML conversion (2 overloads)
- `replaceAssessment(String customCSS)` - Assessment data replacement
- `replaceHTML(Object mainPart, Map<String, List<Object>> replacements)` - HTML replacement
- `getAllElementFromObject(Object obj, Class<?> toSearch)` - DOM traversal
- `replaceImageLinks(String text)` - Image processing

### Medium-Low Priority Methods (Frequent Calls)
- `getFilteredVulns(String section)` - Vulnerability filtering
- `replacement(String content)` - Content replacement
- `replaceFigureVariables(String text, int index)` - Figure variable replacement

## Implementation Architecture

### 1. MethodProfiler Utility Class

**File**: `src/com/fuse/utils/MethodProfiler.java`

**Features**:
- Thread-safe statistics collection using `ConcurrentHashMap` and `AtomicLong`
- Lightweight `ProfileContext` for method timing
- Static enable/disable toggle
- Console-based reporting
- Nanosecond precision with millisecond display

**Key Components**:
```java
public class MethodProfiler {
    // Thread-safe storage
    private static final ConcurrentHashMap<String, MethodStats> methodStats;
    
    // Enable/disable toggle
    private static volatile boolean PROFILING_ENABLED = true;
    
    // Main API methods
    public static ProfileContext start(String methodName);
    public static void printReport();
    public static void setEnabled(boolean enabled);
    public static void clearStats();
}
```

### 2. ProfileContext Pattern

**Usage Pattern**:
```java
// At method start
MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.methodName");
try {
    // Method implementation
} finally {
    profile.end(); // Always record timing
}
```

### 3. Statistics Collection

**MethodStats Class**:
- Call count (`AtomicLong`)
- Total duration (`AtomicLong`)
- Min/Max duration (`AtomicLong` with compare-and-set)
- Calculated average duration

## Implementation Steps

### Step 1: Create MethodProfiler Class
- Implement thread-safe statistics collection
- Create ProfileContext for method timing
- Add enable/disable functionality
- Implement console reporting with formatted output

### Step 2: Add Profiling Annotation (Optional)
Create `@ProfileMethod` annotation for documentation:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ProfileMethod {
    String value() default "";
}
```

### Step 3: Instrument DocxUtils Methods

**High Priority Methods**:
```java
public WordprocessingMLPackage generateDocx(String customCSS) throws Exception {
    MethodProfiler.ProfileContext profile = MethodProfiler.start("DocxUtils.generateDocx");
    try {
        // existing implementation
        return mlp;
    } finally {
        profile.end();
    }
}
```

**Apply similar pattern to**:
- `checkTables()`
- `setFindings()`
- `wrapHTML()` (both overloads)
- `replaceAssessment()`
- `replaceHTML()`
- Other identified methods

### Step 4: Configuration Management

**Static Configuration**:
- Default: enabled
- Runtime toggle: `MethodProfiler.setEnabled(false)`
- Clear stats: `MethodProfiler.clearStats()`

### Step 5: Reporting Implementation

**Console Report Format**:
```
=== DocxUtils Method Profiling Report ===
Method                                     Calls      Total (ms)        Avg (ms)        Min (ms)        Max (ms)
==============================================================================================================================
DocxUtils.generateDocx                         5          2,450.23         490.05           245.12         1,205.67
DocxUtils.checkTables                         15          1,234.56          82.30            12.45           456.78
DocxUtils.wrapHTML                           128            567.89           4.44             0.12            89.32
...
==============================================================================================================================
Total Methods: 12 | Total Calls: 1,543 | Total Time: 5,432.10 ms
=== End Report ===
```

### Step 6: Usage Examples

**Basic Usage**:
```java
// Enable profiling (default)
MethodProfiler.setEnabled(true);

// Generate document (profiling happens automatically)
DocxUtils docxUtils = new DocxUtils(mlp, assessment);
WordprocessingMLPackage result = docxUtils.generateDocx(customCSS);

// Print performance report
MethodProfiler.printReport();

// Clear stats for next run
MethodProfiler.clearStats();
```

**Conditional Usage**:
```java
// Only enable in development/debugging
if (System.getProperty("profile.docx", "false").equals("true")) {
    MethodProfiler.setEnabled(true);
}
```

## Testing Strategy

### Unit Test Coverage
- Test ProfileContext timing accuracy
- Verify thread safety with concurrent access
- Test enable/disable functionality
- Validate report formatting

### Integration Testing
- Profile actual DocxUtils operations
- Test with various document sizes
- Verify minimal performance overhead when disabled
- Test memory usage with large numbers of calls

## Performance Considerations

### Overhead Analysis
- **Enabled**: ~100-200 nanoseconds per method call
- **Disabled**: ~5-10 nanoseconds per method call (boolean check only)
- **Memory**: ~100 bytes per unique method name
- **Thread Safety**: Lock-free implementation using atomic operations

### Best Practices
- Use try-finally blocks to ensure profiling completion
- Keep method names consistent and descriptive
- Clear stats periodically in long-running applications
- Consider sampling for extremely high-frequency methods

## Future Enhancements (Optional)

### Advanced Features
- File-based reporting
- JSON/CSV export formats
- Method call tracing and stack analysis
- Integration with external monitoring tools
- Configurable sampling rates
- Memory usage profiling

### JVM Integration
- JMX beans for monitoring
- Spring Boot Actuator endpoints
- Micrometer metrics integration

## Implementation Priority

1. **Phase 1**: Core MethodProfiler class with basic timing
2. **Phase 2**: Instrument 5-10 most critical DocxUtils methods
3. **Phase 3**: Add comprehensive reporting and configuration
4. **Phase 4**: Full method coverage and testing
5. **Phase 5**: Documentation and usage examples

## Expected Outcomes

- **Visibility**: Clear understanding of which methods are performance bottlenecks
- **Metrics**: Precise timing data for optimization efforts
- **Debugging**: Easy identification of performance regressions
- **Monitoring**: Ongoing performance tracking in development/testing environments

This solution provides a lightweight, developer-friendly way to monitor DocxUtils performance with minimal code changes and zero external dependencies.