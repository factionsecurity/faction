package com.fuse.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight method profiler for tracking method call counts and execution
 * duration.
 * Simple static configuration with console output.
 */
public class MethodProfiler {

    // Enable/disable profiling - can be toggled at runtime
    private static volatile boolean PROFILING_ENABLED = true;

    // Thread-safe storage for method statistics
    private static final ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();

    /**
     * Helper method to repeat a character (for Java 8 compatibility)
     */
    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

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

        long getCallCount() {
            return callCount.get();
        }

        long getTotalDuration() {
            return totalDuration.get();
        }

        long getMinDuration() {
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        long getMaxDuration() {
            return maxDuration.get();
        }

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
     * 
     * @param className  The class name
     * @param methodName The method name
     * @return ProfileContext to end profiling
     */
    public static ProfileContext start(String className, String methodName) {
        String fullMethodName = className + "." + methodName;
        return new ProfileContext(fullMethodName, PROFILING_ENABLED);
    }

    /**
     * Start profiling a method with just the method name
     * 
     * @param methodName The method name
     * @return ProfileContext to end profiling
     */
    public static ProfileContext start(String methodName) {
        return new ProfileContext(methodName, PROFILING_ENABLED);
    }

    /**
     * Enable or disable profiling
     * 
     * @param enabled true to enable profiling, false to disable
     */
    public static void setEnabled(boolean enabled) {
        PROFILING_ENABLED = enabled;
    }

    /**
     * Check if profiling is enabled
     * 
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
        System.out.println(repeatChar('=', 130));

        // Sort methods by total duration (descending)
        List<Map.Entry<String, MethodStats>> sortedEntries = new ArrayList<>(methodStats.entrySet());
        sortedEntries
                .sort((e1, e2) -> Long.compare(e2.getValue().getTotalDuration(), e1.getValue().getTotalDuration()));

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
        System.out.println(repeatChar('=', 130));
        long totalCalls = methodStats.values().stream().mapToLong(MethodStats::getCallCount).sum();
        double totalTimeMs = methodStats.values().stream().mapToLong(MethodStats::getTotalDuration).sum() / 1_000_000.0;

        System.out.printf("Total Methods: %d | Total Calls: %d | Total Time: %.2f ms%n",
                methodStats.size(), totalCalls, totalTimeMs);
        System.out.println("=== End Report ===\n");
    }

    /**
     * Get statistics for a specific method
     * 
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

    /**
     * Get the number of calls for a specific method
     * 
     * @param methodName the method name to query
     * @return number of calls, or 0 if method not found
     */
    public static long getCallCount(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        return stats != null ? stats.getCallCount() : 0;
    }

    /**
     * Get the total execution time for a specific method
     * 
     * @param methodName the method name to query
     * @return total time in milliseconds, or 0 if method not found
     */
    public static long getTotalTime(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        return stats != null ? (long) (stats.getTotalDuration() / 1_000_000.0) : 0;
    }

    /**
     * Get the average execution time for a specific method
     * 
     * @param methodName the method name to query
     * @return average time in milliseconds, or 0 if method not found or no calls
     *         made
     */
    public static long getAverageTime(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        return stats != null ? (long) (stats.getAverageDuration() / 1_000_000.0) : 0;
    }

    /**
     * Get the minimum execution time for a specific method
     * 
     * @param methodName the method name to query
     * @return minimum time in milliseconds, or 0 if method not found
     */
    public static long getMinTime(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        return stats != null ? (long) (stats.getMinDuration() / 1_000_000.0) : 0;
    }

    /**
     * Get the maximum execution time for a specific method
     * 
     * @param methodName the method name to query
     * @return maximum time in milliseconds, or 0 if method not found
     */
    public static long getMaxTime(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        return stats != null ? (long) (stats.getMaxDuration() / 1_000_000.0) : 0;
    }

    /**
     * Check if a method has been profiled (has any recorded calls)
     * 
     * @param methodName the method name to check
     * @return true if the method has been called at least once, false otherwise
     */
    public static boolean hasStats(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        return stats != null && stats.getCallCount() > 0;
    }
}
