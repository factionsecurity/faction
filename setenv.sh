#!/bin/bash
# Tomcat JVM Configuration for Memory Profiling
# Place this file in your Tomcat's bin/ directory

# Set heap size deliberately small to make memory issues visible faster
# In production you have 10GB for 16 users, here we'll use 2GB to simulate constraints
export CATALINA_OPTS="$CATALINA_OPTS -Xms2g -Xmx2g"

# Enable detailed GC logging (Java 9+ syntax)
export CATALINA_OPTS="$CATALINA_OPTS -Xlog:gc*:file=logs/gc.log:time,uptime,level,tags"

# For Java 8, use this instead:
# export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:logs/gc.log"

# Enable heap dump on OutOfMemoryError
export CATALINA_OPTS="$CATALINA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
export CATALINA_OPTS="$CATALINA_OPTS -XX:HeapDumpPath=logs/heap-dump.hprof"

# Enable JMX for remote monitoring (VisualVM, JConsole)
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.port=9090"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.local.only=false"
export CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=localhost"

# Enable Native Memory Tracking
export CATALINA_OPTS="$CATALINA_OPTS -XX:NativeMemoryTracking=detail"

# Use G1GC (better for large heaps)
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxGCPauseMillis=200"

# Enable String deduplication (saves memory)
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseStringDeduplication"

echo "========================================="
echo "JVM Memory Profiling Configuration"
echo "========================================="
echo "Heap Size: 2GB (Xms=Xmx)"
echo "GC Log: logs/gc.log"
echo "JMX Port: 9090"
echo "Heap Dump on OOM: logs/heap-dump.hprof"
echo "========================================="
