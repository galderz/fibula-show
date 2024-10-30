# Unfibula

## Research Progress

### Experiment 005

Is the generated native image configuration deterministic?
That is, if neither the code nor the graalvm version changes,
does repeated invocations of the agent generate the same configuration?

Also, what happens with the merged configuration?

```shell
$ NATIVE_AGENT=true make run-jvm
$ make copy-native-config-runner
$ make copy-native-config-forked
$ make merge-native-config
```

### Experiment 004

Merge native image configurations for runner and forked processes using `native-image-configure`,
then rebuild the native executable.

Re-run the benchmark in jvm mode with the agent,
copy the configurations for the runner and forked processes,
and merge them:
```shell
$ NATIVE_AGENT=true make run-jvm

$ make copy-native-config-runner
mkdir -p src/main/resources/runner-native-config
runner_dir=$(ls -d target/native-agent-config-* | sort -V | head -n 1)
cp -r $runner_dir/* src/main/resources/runner-native-config

$ make copy-native-config-forked
mkdir -p src/main/resources/forked-native-config
forked_dir=$(ls -d target/native-agent-config-* | sort -V | head -n 2 | tail -n 1)
cp -r $forked_dir/* src/main/resources/forked-native-config

$ make merge-native-config
mkdir -p src/main/resources/META-INF/native-image
/Users/galder/opt/graal-21/bin/native-image-configure \
   generate \
   --input-dir=src/main/resources/runner-native-config \
   --input-dir=src/main/resources/forked-native-config \
   --output-dir=src/main/resources/META-INF/native-image
```

Rebuild the benchmarks.jar to include the native image configuration,
rebuild the native application from the jar,
and try running it:

```shell
$ make
JAVA_HOME=/Users/galder/opt/java-21 /Users/galder/opt/maven/bin/mvn package
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------< org.sample:unfibula >-------------------------
[INFO] Building JMH benchmark sample: Java 1.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- resources:2.6:resources (default-resources) @ unfibula ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 6 resources
[INFO]
[INFO] --- compiler:3.8.0:compile (default-compile) @ unfibula ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- resources:2.6:testResources (default-testResources) @ unfibula ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/galder/1/fibula-show/2410-unfibula/unfibula/src/test/resources
[INFO]
[INFO] --- compiler:3.8.0:testCompile (default-testCompile) @ unfibula ---
[INFO] No sources to compile
[INFO]
[INFO] --- surefire:2.17:test (default-test) @ unfibula ---
[INFO] No tests to run.
[INFO]
[INFO] --- jar:2.4:jar (default-jar) @ unfibula ---
[INFO] Building jar: /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/unfibula-1.0.jar
[INFO]
[INFO] --- shade:3.2.1:shade (default) @ unfibula ---
[INFO] Including org.openjdk.jmh:jmh-core:jar:1.37 in the shaded jar.
[INFO] Including net.sf.jopt-simple:jopt-simple:jar:5.0.4 in the shaded jar.
[INFO] Including org.apache.commons:commons-math3:jar:3.6.1 in the shaded jar.
[INFO] Replacing /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks.jar with /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/unfibula-1.0-shaded.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.769 s
[INFO] Finished at: 2024-10-30T14:37:37+01:00
[INFO] ------------------------------------------------------------------------
/Users/galder/opt/graal-21/bin/native-image --no-fallback -jar target/benchmarks.jar target/benchmarks
========================================================================================================================
GraalVM Native Image: Generating 'benchmarks' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (3.1s @ 0.09GB)
 Java version: 21.0.2+13, vendor version: GraalVM CE 21.0.2+13.1
 Graal compiler: optimization level: 2, target machine: armv8-a
 C compiler: cc (apple, arm64, 15.0.0)
 Garbage collector: Serial GC (max heap size: 80% of RAM)
 1 user-specific feature(s):
 - com.oracle.svm.thirdparty.gson.GsonFeature
------------------------------------------------------------------------------------------------------------------------
Build resources:
 - 24.18GB of memory (75.6% of 32.00GB system memory, determined at start)
 - 10 thread(s) (100.0% of 10 available processor(s), determined at start)
[2/8] Performing analysis...  [****]                                                                     (7.8s @ 0.38GB)
    4,790 reachable types   (78.7% of    6,083 total)
    6,563 reachable fields  (46.6% of   14,072 total)
   23,434 reachable methods (51.2% of   45,741 total)
    1,665 types,   692 fields, and 2,273 methods registered for reflection
       65 types,    71 fields, and    60 methods registered for JNI access
        5 native libraries: -framework CoreServices, -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.2s @ 0.54GB)
[4/8] Parsing methods...      [*]                                                                        (0.8s @ 0.53GB)
[5/8] Inlining methods...     [***]                                                                      (0.8s @ 0.36GB)
[6/8] Compiling methods...    [***]                                                                      (7.1s @ 0.40GB)
[7/8] Layouting methods...    [*]                                                                        (1.2s @ 0.55GB)
[8/8] Creating image...       [**]                                                                       (2.3s @ 0.60GB)
   8.77MB (42.44%) for code area:    14,381 compilation units
  11.41MB (55.20%) for image heap:  141,308 objects and 50 resources
 498.74kB ( 2.36%) for other data
  20.66MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   6.34MB java.base                                            2.76MB byte[] for code metadata
   1.13MB svm.jar (Native Image)                               1.88MB byte[] for java.lang.String
 797.34kB benchmarks.jar                                       1.37MB java.lang.String
 114.46kB java.logging                                         1.10MB java.lang.Class
  56.80kB org.graalvm.nativeimage.base                       411.64kB com.oracle.svm.core.hub.DynamicHubCompanion
  50.59kB jdk.proxy1                                         344.70kB byte[] for reflection metadata
  48.91kB jdk.crypto.ec                                      304.68kB byte[] for general heap data
  48.74kB jdk.proxy3                                         278.11kB java.util.HashMap$Node
  25.57kB jdk.net                                            274.10kB java.lang.String[]
  24.47kB jdk.internal.reflect                               264.36kB heap alignment
  76.98kB for 8 more packages                                  2.47MB for 1186 more object types
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                        1.8s (7.1% of total time) in 195 GCs | Peak RSS: 1.17GB | CPU load: 5.96
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks (executable)
========================================================================================================================
Finished generating 'benchmarks' in 24.7s.

$ target/benchmarks
Exception in thread "main" java.lang.ExceptionInInitializerError
	at org.openjdk.jmh.runner.Runner.newBenchmarkParams(Runner.java:424)
	at org.openjdk.jmh.runner.Runner.getActionPlans(Runner.java:352)
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:543)
	at org.openjdk.jmh.runner.Runner.internalRun(Runner.java:309)
	at org.openjdk.jmh.runner.Runner.run(Runner.java:208)
	at org.openjdk.jmh.Main.main(Main.java:71)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
Caused by: java.lang.IllegalStateException: Consistency check failed for type, off = 16, markerBegin = 12, markerEnd = 168
	at org.openjdk.jmh.util.Utils.check(Utils.java:375)
	at org.openjdk.jmh.util.Utils.check(Utils.java:365)
	at org.openjdk.jmh.infra.IterationParams.<clinit>(IterationParams.java:52)
	... 7 more
make: *** [Makefile:36: run] Error 1
```

An `IllegalStateException` is thrown at runtime.
This is probably due to runtime/buildtime initialization.

### Experiment 003

Run jvm mode with native agent to capture configuration.

Initially tried to pass in `-agentlib:native-image-agent=config-output-dir=target/native-agent-config`,
but that alone creates errors like this because JMH's runner and forked processes are trying to write into the same folder.

```shell
$ java -agentlib:native-image-agent=config-output-dir=target/native-agent-config -jar target/benchmarks.jar -f 1 -r 1 -w 1 -i 2 -wi 2
native-image-agent: Error: Output directory 'target/native-agent-config' is locked by process 46094,
which means another agent instance is already writing to this directory.
Only one agent instance can safely write to a specific target directory at the same time.
Unless file '.lock' is a leftover from an earlier process that terminated abruptly, it is unsafe to delete it.
For running multiple processes with agents at the same time to create a single configuration,
read AutomaticMetadataCollection.md or https://www.graalvm.org/dev/reference-manual/native-image/metadata/AutomaticMetadataCollection/ on how to use the native-image-configure tool. 
```

Looked at the documentation and passed `-agentlib:native-image-agent=config-output-dir=target/native-agent-config-{pid}-{datetime}` instead.
That creates multiple folders, one of the runner process and another for the forked one:

```shell
$ NATIVE_AGENT=true make run-jvm
JAVA_HOME=/Users/galder/opt/java-21 /Users/galder/opt/maven/bin/mvn package
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------< org.sample:unfibula >-------------------------
[INFO] Building JMH benchmark sample: Java 1.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- resources:2.6:resources (default-resources) @ unfibula ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/galder/1/fibula-show/2410-unfibula/unfibula/src/main/resources
[INFO]
[INFO] --- compiler:3.8.0:compile (default-compile) @ unfibula ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/classes
[INFO]
[INFO] --- resources:2.6:testResources (default-testResources) @ unfibula ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/galder/1/fibula-show/2410-unfibula/unfibula/src/test/resources
[INFO]
[INFO] --- compiler:3.8.0:testCompile (default-testCompile) @ unfibula ---
[INFO] No sources to compile
[INFO]
[INFO] --- surefire:2.17:test (default-test) @ unfibula ---
[INFO] No tests to run.
[INFO]
[INFO] --- jar:2.4:jar (default-jar) @ unfibula ---
[INFO] Building jar: /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/unfibula-1.0.jar
[INFO]
[INFO] --- shade:3.2.1:shade (default) @ unfibula ---
[INFO] Including org.openjdk.jmh:jmh-core:jar:1.37 in the shaded jar.
[INFO] Including net.sf.jopt-simple:jopt-simple:jar:5.0.4 in the shaded jar.
[INFO] Including org.apache.commons:commons-math3:jar:3.6.1 in the shaded jar.
[INFO] Replacing /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks.jar with /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/unfibula-1.0-shaded.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.207 s
[INFO] Finished at: 2024-10-30T11:30:26+01:00
[INFO] ------------------------------------------------------------------------
/Users/galder/opt/graal-21/bin/java -agentlib:native-image-agent=config-output-dir=target/native-agent-config-{pid}-{datetime} -jar target/benchmarks.jar -f 1 -r 1 -w 1 -i 2 -wi 2
# JMH version: 1.37
# VM version: JDK 21.0.2, OpenJDK 64-Bit Server VM, 21.0.2+13-jvmci-23.1-b30
# VM invoker: /Users/galder/opt/graalvm-community-openjdk-21.0.2+13.1/Contents/Home/bin/java
# VM options: -XX:ThreadPriorityPolicy=1 -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCIProduct -XX:-UnlockExperimentalVMOptions -agentlib:native-image-agent=config-output-dir=target/native-agent-config-{pid}-{datetime}
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 2 iterations, 1 s each
# Measurement: 2 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: org.sample.MyBenchmark.testMethod

# Run progress: 0.00% complete, ETA 00:00:04
# Fork: 1 of 1
OpenJDK 64-Bit Server VM warning: -XX:ThreadPriorityPolicy=1 may require system level permission, e.g., being the root user. If the necessary permission is not possessed, changes to priority will be silently ignored.
# Warmup Iteration   1: 1777541732.645 ops/s
# Warmup Iteration   2: 1773954859.471 ops/s
Iteration   1: 1543273989.872 ops/s
Iteration   2: 1773523240.089 ops/s


Result "org.sample.MyBenchmark.testMethod":
  1658398614.980 ops/s


# Run complete. Total time: 00:00:04

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

NOTE: Current JVM experimentally supports Compiler Blackholes, and they are in use. Please exercise
extra caution when trusting the results, look into the generated code to check the benchmark still
works, and factor in a small probability of new VM bugs. Additionally, while comparisons between
different JVMs are already problematic, the performance difference caused by different Blackhole
modes can be very significant. Please make sure you use the consistent Blackhole mode for comparisons.

Benchmark                Mode  Cnt           Score   Error  Units
MyBenchmark.testMethod  thrpt    2  1658398614.980          ops/s

$ ls target
total 2.8M
drwxr-xr-x 10 galder staff  320 Oct 30 11:30 .
drwxr-xr-x  7 galder staff  224 Oct 30 11:30 ..
drwxr-xr-x  4 galder staff  128 Oct 30 11:30 classes
drwxr-xr-x  3 galder staff   96 Oct 30 11:30 generated-sources
drwxr-xr-x  3 galder staff   96 Oct 30 11:30 maven-archiver
drwxr-xr-x  3 galder staff   96 Oct 30 11:30 maven-status
drwxr-xr-x  9 galder staff  288 Oct 30 11:30 native-agent-config-46609-20241030T103026Z
drwxr-xr-x  9 galder staff  288 Oct 30 11:30 native-agent-config-46613-20241030T103026Z
-rw-r--r--  1 galder staff 2.8M Oct 30 11:30 benchmarks.jar
-rw-r--r--  1 galder staff  14K Oct 30 11:30 unfibula-1.0.jar
```

### Experiment 002

Add flag to avoid fallback:

```shell
$ native-image --no-fallback -jar benchmarks.jar target/benchmarks
========================================================================================================================
GraalVM Native Image: Generating 'benchmarks' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (2.7s @ 0.09GB)
 Java version: 21.0.2+13, vendor version: GraalVM CE 21.0.2+13.1
 Graal compiler: optimization level: 2, target machine: armv8-a
 C compiler: cc (apple, arm64, 15.0.0)
 Garbage collector: Serial GC (max heap size: 80% of RAM)
 1 user-specific feature(s):
 - com.oracle.svm.thirdparty.gson.GsonFeature
------------------------------------------------------------------------------------------------------------------------
Build resources:
 - 24.18GB of memory (75.6% of 32.00GB system memory, determined at start)
 - 10 thread(s) (100.0% of 10 available processor(s), determined at start)
[2/8] Performing analysis...  [****]                                                                     (7.2s @ 0.40GB)
    4,500 reachable types   (77.5% of    5,803 total)
    5,838 reachable fields  (48.4% of   12,072 total)
   21,823 reachable methods (50.1% of   43,581 total)
    1,506 types,    91 fields, and   894 methods registered for reflection
       60 types,    60 fields, and    53 methods registered for JNI access
        4 native libraries: -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.2s @ 0.49GB)
[4/8] Parsing methods...      [*]                                                                        (0.7s @ 0.49GB)
[5/8] Inlining methods...     [***]                                                                      (0.7s @ 0.29GB)
[6/8] Compiling methods...    [***]                                                                      (6.5s @ 0.42GB)
[7/8] Layouting methods...    [*]                                                                        (1.2s @ 0.49GB)
[8/8] Creating image...       [**]                                                                       (2.4s @ 0.38GB)
   7.95MB (42.45%) for code area:    12,819 compilation units
  10.33MB (55.12%) for image heap:  131,645 objects and 48 resources
 465.77kB ( 2.43%) for other data
  18.74MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   5.94MB java.base                                            2.50MB byte[] for code metadata
   1.03MB svm.jar (Native Image)                               1.74MB byte[] for java.lang.String
 539.89kB benchmarks.jar                                       1.28MB java.lang.String
 114.00kB java.logging                                         1.02MB java.lang.Class
  56.80kB org.graalvm.nativeimage.base                       386.72kB com.oracle.svm.core.hub.DynamicHubCompanion
  48.91kB jdk.crypto.ec                                      303.01kB byte[] for general heap data
  43.64kB jdk.proxy1                                         274.50kB java.util.HashMap$Node
  42.03kB jdk.proxy3                                         253.23kB java.lang.String[]
  25.66kB jdk.net                                            249.06kB java.lang.Object[]
  21.98kB org.graalvm.collections                            217.18kB byte[] for reflection metadata
  41.61kB for 6 more packages                                  2.15MB for 1110 more object types
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                        1.9s (8.0% of total time) in 220 GCs | Peak RSS: 1.08GB | CPU load: 6.66
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks (executable)
========================================================================================================================
Finished generating 'benchmarks' in 22.9s.

$ target/benchmarks
Exception in thread "main" java.lang.ExceptionInInitializerError
	at org.openjdk.jmh.runner.options.CommandLineOptions.<init>(CommandLineOptions.java:99)
	at org.openjdk.jmh.Main.main(Main.java:41)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
Caused by: java.lang.IllegalArgumentException: int is not a value type
	at joptsimple.internal.Reflection.findConverter(Reflection.java:66)
	at org.openjdk.jmh.runner.options.IntegerValueConverter.<clinit>(IntegerValueConverter.java:35)
	... 3 more
make: *** [Makefile:28: run] Error 1
```

Oddly enough,
when requested no fallback,
the native image build completed but failed at runtime.

### Experiment 001

What happens if you take a stock build `benchmarks.jar` and try to produce a native image out of it?

```shell
$ native-image -jar benchmarks.jar
========================================================================================================================
GraalVM Native Image: Generating 'benchmarks' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (3.0s @ 0.09GB)
 Java version: 21.0.2+13, vendor version: GraalVM CE 21.0.2+13.1
 Graal compiler: optimization level: 2, target machine: armv8-a
 C compiler: cc (apple, arm64, 15.0.0)
 Garbage collector: Serial GC (max heap size: 80% of RAM)
 1 user-specific feature(s):
 - com.oracle.svm.thirdparty.gson.GsonFeature
------------------------------------------------------------------------------------------------------------------------
Build resources:
 - 24.18GB of memory (75.6% of 32.00GB system memory, determined at start)
 - 10 thread(s) (100.0% of 10 available processor(s), determined at start)
[2/8] Performing analysis...  [***]                                                                      (6.8s @ 0.35GB)
    4,500 reachable types   (77.5% of    5,803 total)
    5,838 reachable fields  (48.4% of   12,072 total)
   21,824 reachable methods (50.1% of   43,570 total)
    1,506 types,    91 fields, and   894 methods registered for reflection
       60 types,    60 fields, and    53 methods registered for JNI access
        4 native libraries: -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.2s @ 0.44GB)

Warning: Resource access method java.lang.ClassLoader.getResources invoked at org.openjdk.jmh.runner.AbstractResourceReader.getReaders(AbstractResourceReader.java:68)
Warning: Aborting stand-alone image build due to accessing resources without configuration.
------------------------------------------------------------------------------------------------------------------------
                        0.8s (7.3% of total time) in 85 GCs | Peak RSS: 0.87GB | CPU load: 6.05
========================================================================================================================
Finished generating 'benchmarks' in 11.1s.
Generating fallback image...
Warning: Image 'benchmarks' is a fallback image that requires a JDK for execution (use --no-fallback to suppress fallback image generation and to print more detailed information why a fallback image was necessary).
```

Lack of native configuration halts it and falls back in jvm mode.

Two immediate things to add:

1. Do not fall back into jvm mode, and instead fail the native image process if configuration lacks.
2. Run jvm mode with native image agent and see what configuration splits out.

## Makefile Guide

### Build a native executable

```shell
make
```

### Run a benchmark in JVM mode

```shell
make run-jvm
```
