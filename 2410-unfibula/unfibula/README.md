# Unfibula

## Research Progress

### Experiment 010

Configure native image invocation so that `org.openjdk.jmh.runner.ForkedMain` runs in the forked process
rather than `org.mendrugo.fibula.RunnerMain`.
To do that, switch the `native-image` invocation from `-jar <jar>`
to `-cp <jar> org.openjdk.jmh.runner.ForkedMain`:

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
[INFO] Copying 12 resources
[INFO]
[INFO] --- compiler:3.8.0:compile (default-compile) @ unfibula ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 4 source files to /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/classes
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
[INFO] Total time:  1.432 s
[INFO] Finished at: 2024-11-01T11:56:02+01:00
[INFO] ------------------------------------------------------------------------
/Users/galder/opt/graal-21/bin/native-image --no-fallback -cp target/benchmarks.jar org.openjdk.jmh.runner.ForkedMain target/benchmarks
========================================================================================================================
GraalVM Native Image: Generating 'benchmarks' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (3.2s @ 0.09GB)
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
[2/8] Performing analysis...  [***]                                                                      (6.7s @ 0.32GB)
    4,195 reachable types   (76.4% of    5,488 total)
    5,580 reachable fields  (43.9% of   12,721 total)
   20,487 reachable methods (49.3% of   41,543 total)
    1,402 types,   686 fields, and 2,133 methods registered for reflection
       63 types,    69 fields, and    59 methods registered for JNI access
        5 native libraries: -framework CoreServices, -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.1s @ 0.42GB)
[4/8] Parsing methods...      [*]                                                                        (0.7s @ 0.42GB)
[5/8] Inlining methods...     [***]                                                                      (0.5s @ 0.47GB)
[6/8] Compiling methods...    [***]                                                                      (6.0s @ 0.36GB)
[7/8] Layouting methods...    [*]                                                                        (1.0s @ 0.49GB)
[8/8] Creating image...       [**]                                                                       (2.1s @ 0.40GB)
   7.16MB (39.76%) for code area:    12,406 compilation units
  10.41MB (57.77%) for image heap:  128,225 objects and 47 resources
 455.63kB ( 2.47%) for other data
  18.01MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   5.17MB java.base                                            2.25MB byte[] for code metadata
   1.10MB svm.jar (Native Image)                               1.72MB byte[] for java.lang.String
 449.01kB benchmarks.jar                                       1.25MB java.lang.String
 110.50kB java.logging                                       979.12kB java.lang.Class
  56.80kB org.graalvm.nativeimage.base                       535.10kB heap alignment
  50.59kB jdk.proxy1                                         360.51kB com.oracle.svm.core.hub.DynamicHubCompanion
  48.74kB jdk.proxy3                                         312.11kB byte[] for reflection metadata
  25.57kB jdk.net                                            296.18kB byte[] for general heap data
  24.47kB jdk.internal.reflect                               268.73kB java.util.HashMap$Node
  21.98kB org.graalvm.collections                            254.02kB java.lang.Object[]
  54.91kB for 7 more packages                                  2.26MB for 1119 more object types
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                        1.6s (7.2% of total time) in 295 GCs | Peak RSS: 1.01GB | CPU load: 6.43
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks (executable)
========================================================================================================================
Finished generating 'benchmarks' in 21.7s.
/Users/galder/opt/java-21/bin/java  -jar target/benchmarks.jar -f 1 -r 1 -w 1 -i 2 -wi 2
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 21.0.2, Substrate VM, GraalVM CE 21.0.2+13.1
# *** WARNING: This VM is not supported by JMH. The produced benchmark data can be completely wrong.
# VM invoker: target/benchmarks
# VM options: <none>
# Compiler hints: disabled (Forced off)
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 2 iterations, 1 s each
# Measurement: 2 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: org.sample.MyBenchmark.testMethod

# Run progress: 0.00% complete, ETA 00:00:04
# Fork: 1 of 1
Exception in thread "main" java.lang.IllegalArgumentException: Expected two arguments for forked VM
	at org.openjdk.jmh.runner.ForkedMain.main(ForkedMain.java:56)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
<forked VM failed with exit code 1>
<stdout last='20 lines'>
</stdout>
<stderr last='20 lines'>
Exception in thread "main" java.lang.IllegalArgumentException: Expected two arguments for forked VM
	at org.openjdk.jmh.runner.ForkedMain.main(ForkedMain.java:56)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
</stderr>

# Run complete. Total time: 00:00:00

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

Benchmark  Mode  Cnt  Score   Error  Units
```

### Experiment 009

Swap the runner main for a custom one that allows switching the native executable after the call to `Utils.readPropertiesFromCommand`.
To do that, swapped the `mainClass` attribute of the `maven-shade-plugin`
from `org.openjdk.jmh.Main` to `org.mendrugo.fibula.RunnerMain`.
This new main creates a `org.openjdk.jmh.runner.Runner` instance with a custom `org.openjdk.jmh.runner.format.OutputFormat`.
The custom `OutputFormat` overrides `startBenchmark` to adjust the `jvm` parameter to be
either `java` or the native executable depending on whether the native executable is present or not.
This method makes additional changes into `BenchmarkParams` in order to adjust `VM version` output and other data.
With these changes:

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
[INFO] Copying 12 resources
[INFO]
[INFO] --- compiler:3.8.0:compile (default-compile) @ unfibula ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 4 source files to /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/classes
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
[INFO] Total time:  1.341 s
[INFO] Finished at: 2024-11-01T11:00:29+01:00
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
[2/8] Performing analysis...  [*****]                                                                    (8.0s @ 0.40GB)
    4,810 reachable types   (78.9% of    6,099 total)
    6,588 reachable fields  (46.7% of   14,113 total)
   23,508 reachable methods (51.3% of   45,869 total)
    1,669 types,   688 fields, and 2,270 methods registered for reflection
       64 types,    71 fields, and    59 methods registered for JNI access
        5 native libraries: -framework CoreServices, -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.2s @ 0.47GB)
[4/8] Parsing methods...      [*]                                                                        (0.7s @ 0.53GB)
[5/8] Inlining methods...     [***]                                                                      (0.7s @ 0.32GB)
[6/8] Compiling methods...    [***]                                                                      (6.6s @ 0.43GB)
[7/8] Layouting methods...    [*]                                                                        (1.4s @ 0.47GB)
[8/8] Creating image...       [**]                                                                       (2.2s @ 0.56GB)
   8.80MB (42.49%) for code area:    14,432 compilation units
  11.42MB (55.15%) for image heap:  141,540 objects and 48 resources
 499.68kB ( 2.36%) for other data
  20.71MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   6.35MB java.base                                            2.77MB byte[] for code metadata
   1.13MB svm.jar (Native Image)                               1.89MB byte[] for java.lang.String
 812.94kB benchmarks.jar                                       1.37MB java.lang.String
 114.46kB java.logging                                         1.10MB java.lang.Class
  56.80kB org.graalvm.nativeimage.base                       413.36kB com.oracle.svm.core.hub.DynamicHubCompanion
  50.59kB jdk.proxy1                                         344.82kB byte[] for reflection metadata
  48.91kB jdk.crypto.ec                                      304.68kB byte[] for general heap data
  48.74kB jdk.proxy3                                         278.11kB java.util.HashMap$Node
  25.57kB jdk.net                                            274.69kB java.lang.String[]
  24.47kB jdk.internal.reflect                               256.96kB java.lang.Object[]
  76.98kB for 8 more packages                                  2.47MB for 1195 more object types
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                        1.9s (7.7% of total time) in 309 GCs | Peak RSS: 1.08GB | CPU load: 6.30
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks (executable)
========================================================================================================================
Finished generating 'benchmarks' in 24.1s.

$ /Users/galder/opt/java-21/bin/java  -jar target/benchmarks.jar -f 1 -r 1 -w 1 -i 2 -wi 2
SUBSTRATE
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 21.0.2, Substrate VM, GraalVM CE 21.0.2+13.1
# *** WARNING: This VM is not supported by JMH. The produced benchmark data can be completely wrong.
# VM invoker: target/benchmarks
# VM options: <none>
# Compiler hints: disabled (Forced off)
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 2 iterations, 1 s each
# Measurement: 2 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: org.sample.MyBenchmark.testMethod

# Run progress: 0.00% complete, ETA 00:00:04
# Fork: 1 of 1
Exception in thread "main" java.lang.IllegalArgumentException: class java.lang.Boolean is not a value type
	at joptsimple.internal.Reflection.findConverter(Reflection.java:66)
	at joptsimple.ArgumentAcceptingOptionSpec.ofType(ArgumentAcceptingOptionSpec.java:106)
	at org.openjdk.jmh.runner.options.CommandLineOptions.<init>(CommandLineOptions.java:146)
	at org.mendrugo.fibula.RunnerMain.main(RunnerMain.java:20)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
<forked VM failed with exit code 1>
<stdout last='20 lines'>
</stdout>
<stderr last='20 lines'>
Exception in thread "main" java.lang.IllegalArgumentException: class java.lang.Boolean is not a value type
	at joptsimple.internal.Reflection.findConverter(Reflection.java:66)
	at joptsimple.ArgumentAcceptingOptionSpec.ofType(ArgumentAcceptingOptionSpec.java:106)
	at org.openjdk.jmh.runner.options.CommandLineOptions.<init>(CommandLineOptions.java:146)
	at org.mendrugo.fibula.RunnerMain.main(RunnerMain.java:20)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
</stderr>

# Run complete. Total time: 00:00:00

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

Benchmark  Mode  Cnt  Score   Error  Units
```

The runner now works fine, but the problems have now shifted to the forked process.
The forked process should be running `org.openjdk.jmh.runner.ForkedMain`,
or a custom version of that if adjustments are necessary.

### Experiment 008

Stop running the native executable directly.
Instead, start with the jar so that the runner runs in JVM mode,
and let future customizations on the runner decide whether to run the forked main as jvm or native executable.
This means that there's no longer need for runner native configuration.
Merging will still be needed because additional forked configuration will be required.

Here is a runner in JVM mode manually tweaked to use the binary as jvm.
This doesn't work because the jvm setting needs to be done later,
e.g. via ~OutputFormat.startBenchmark~ that the original prototype does:

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
[INFO] Copying 24 resources
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
[INFO] Total time:  1.284 s
[INFO] Finished at: 2024-11-01T09:26:32+01:00
[INFO] ------------------------------------------------------------------------
/Users/galder/opt/graal-21/bin/native-image --no-fallback -jar target/benchmarks.jar target/benchmarks
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
[2/8] Performing analysis...  [****]                                                                     (7.8s @ 0.41GB)
    4,790 reachable types   (78.7% of    6,083 total)
    6,561 reachable fields  (46.6% of   14,072 total)
   23,434 reachable methods (51.2% of   45,747 total)
    1,664 types,   688 fields, and 2,270 methods registered for reflection
       64 types,    71 fields, and    59 methods registered for JNI access
        5 native libraries: -framework CoreServices, -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.2s @ 0.44GB)
[4/8] Parsing methods...      [*]                                                                        (0.7s @ 0.55GB)
[5/8] Inlining methods...     [***]                                                                      (0.7s @ 0.37GB)
[6/8] Compiling methods...    [***]                                                                      (6.6s @ 0.44GB)
[7/8] Layouting methods...    [*]                                                                        (1.4s @ 0.47GB)
[8/8] Creating image...       [**]                                                                       (2.2s @ 0.51GB)
   8.77MB (42.44%) for code area:    14,381 compilation units
  11.41MB (55.20%) for image heap:  141,292 objects and 48 resources
 498.77kB ( 2.36%) for other data
  20.66MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   6.34MB java.base                                            2.75MB byte[] for code metadata
   1.13MB svm.jar (Native Image)                               1.88MB byte[] for java.lang.String
 797.36kB benchmarks.jar                                       1.37MB java.lang.String
 114.46kB java.logging                                         1.10MB java.lang.Class
  56.80kB org.graalvm.nativeimage.base                       411.64kB com.oracle.svm.core.hub.DynamicHubCompanion
  50.59kB jdk.proxy1                                         344.47kB byte[] for reflection metadata
  48.91kB jdk.crypto.ec                                      304.68kB byte[] for general heap data
  48.74kB jdk.proxy3                                         278.11kB java.util.HashMap$Node
  25.57kB jdk.net                                            274.10kB java.lang.String[]
  24.47kB jdk.internal.reflect                               267.23kB heap alignment
  76.98kB for 8 more packages                                  2.46MB for 1184 more object types
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                        1.9s (8.0% of total time) in 249 GCs | Peak RSS: 1.06GB | CPU load: 6.42
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks (executable)
========================================================================================================================
Finished generating 'benchmarks' in 23.8s.
/Users/galder/opt/java-21/bin/java  -jar target/benchmarks.jar -jvm target/benchmarks
Exception in thread "main" java.lang.RuntimeException: Unable to extract forked JVM properties using: 'target/benchmarks -cp target/benchmarks.jar org.openjdk.jmh.runner.PrintPropertiesMain'; [Exception in thread "main" java.lang.IllegalArgumentException: class java.lang.Boolean is not a value type
	at joptsimple.internal.Reflection.findConverter(Reflection.java:66)
	at joptsimple.ArgumentAcceptingOptionSpec.ofType(ArgumentAcceptingOptionSpec.java:106)
	at org.openjdk.jmh.runner.options.CommandLineOptions.<init>(CommandLineOptions.java:146)
	at org.openjdk.jmh.Main.main(Main.java:41)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
]
	at org.openjdk.jmh.util.Utils.readPropertiesFromCommand(Utils.java:588)
	at org.openjdk.jmh.runner.Runner.newBenchmarkParams(Runner.java:468)
	at org.openjdk.jmh.runner.Runner.getActionPlans(Runner.java:352)
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:543)
	at org.openjdk.jmh.runner.Runner.internalRun(Runner.java:309)
	at org.openjdk.jmh.runner.Runner.run(Runner.java:208)
	at org.openjdk.jmh.Main.main(Main.java:71)
make: *** [Makefile:36: run] Error 1
```

### Experiment 007

Try to pass in `-jvm` to point to the binary and see how that behaves:

```shell
target/benchmarks -jvm target/benchmarks
Exception in thread "main" java.lang.RuntimeException: Unable to extract forked JVM properties using: 'target/benchmarks -cp  org.openjdk.jmh.runner.PrintPropertiesMain'; [Exception in thread "main" java.util.MissingResourceException: Can't find bundle for base name joptsimple.ExceptionMessages, locale en_001
	at java.base@21.0.2/java.util.ResourceBundle.throwMissingResourceException(ResourceBundle.java:2059)
	at java.base@21.0.2/java.util.ResourceBundle.getBundleImpl(ResourceBundle.java:1697)
	at java.base@21.0.2/java.util.ResourceBundle.getBundleImpl(ResourceBundle.java:1600)
	at java.base@21.0.2/java.util.ResourceBundle.getBundleImpl(ResourceBundle.java:1555)
	at java.base@21.0.2/java.util.ResourceBundle.getBundle(ResourceBundle.java:935)
	at joptsimple.internal.Messages.message(Messages.java:41)
	at joptsimple.OptionException.formattedMessage(OptionException.java:121)
	at joptsimple.OptionException.localizedMessage(OptionException.java:117)
	at joptsimple.OptionException.getMessage(OptionException.java:113)
	at org.openjdk.jmh.runner.options.CommandLineOptions.<init>(CommandLineOptions.java:403)
	at org.openjdk.jmh.Main.main(Main.java:41)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
]
	at org.openjdk.jmh.util.Utils.readPropertiesFromCommand(Utils.java:588)
	at org.openjdk.jmh.runner.Runner.newBenchmarkParams(Runner.java:468)
	at org.openjdk.jmh.runner.Runner.getActionPlans(Runner.java:352)
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:543)
	at org.openjdk.jmh.runner.Runner.internalRun(Runner.java:309)
	at org.openjdk.jmh.runner.Runner.run(Runner.java:208)
	at org.openjdk.jmh.Main.main(Main.java:71)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
make: *** [Makefile:47: run] Error 1
```

It fails unable to extra forked JVM properties out of the binary.

Running the jar file and passing in the native binary,
e.g. `java -jar target/benchmarks.jar -jvm target/benchmarks`
throws the same error above.

How does Fibula get around this?
It does so by swapping the `BenchmarkParameters.jvm` field value at a later stage.
When native Fibula starts, the `jvm` value is still the same value as the runner,
and then before starting the actual benchmark is the moment when `jvm` and VM fields are swapped.
To do this swapping, the runner main class needs to be swapped.

### Experiment 006

To resolve the `IllegalStateException` additional native image configuration is required.
The padded fields present in JMH types need to be kept on,
so that false sharing can be avoided.
A way to solve this is by registering the necessary types for reflection access,
and make sure all the declared fields are set to true.

A folder called `src/main/resources/runner-additional-native-config`,
which contains additional configuration that should be merged in.
It instructs `org.openjdk.jmh.infra.IterationParamsL[0-4]` to have all declared fields registered for reflection.

Then add this folder to the merge and rebuild:

```shell
$ make merge-native-config
mkdir -p src/main/resources/META-INF/native-image
/Users/galder/opt/graal-21/bin/native-image-configure \
   generate \
   --input-dir=src/main/resources/runner-agent-native-config \
   --input-dir=src/main/resources/runner-additional-native-config \
   --input-dir=src/main/resources/forked-agent-native-config \
   --output-dir=src/main/resources/META-INF/native-image

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
[INFO] Copying 24 resources
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
[INFO] Total time:  0.881 s
[INFO] Finished at: 2024-10-30T16:49:45+01:00
[INFO] ------------------------------------------------------------------------
/Users/galder/opt/graal-21/bin/native-image --no-fallback -jar target/benchmarks.jar target/benchmarks
========================================================================================================================
GraalVM Native Image: Generating 'benchmarks' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (3.5s @ 0.09GB)
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
[2/8] Performing analysis...  [****]                                                                     (8.1s @ 0.45GB)
    4,790 reachable types   (78.7% of    6,083 total)
    6,691 reachable fields  (47.5% of   14,072 total)
   23,434 reachable methods (51.2% of   45,729 total)
    1,665 types,   820 fields, and 2,273 methods registered for reflection
       65 types,    71 fields, and    60 methods registered for JNI access
        5 native libraries: -framework CoreServices, -framework Foundation, dl, pthread, z
[3/8] Building universe...                                                                               (1.3s @ 0.46GB)
[4/8] Parsing methods...      [*]                                                                        (0.8s @ 0.58GB)
[5/8] Inlining methods...     [***]                                                                      (0.8s @ 0.42GB)
[6/8] Compiling methods...    [***]                                                                      (7.1s @ 0.43GB)
[7/8] Layouting methods...    [*]                                                                        (1.6s @ 0.45GB)
[8/8] Creating image...       [**]                                                                       (2.4s @ 0.53GB)
   8.77MB (42.44%) for code area:    14,382 compilation units
  11.41MB (55.20%) for image heap:  141,560 objects and 50 resources
 498.74kB ( 2.36%) for other data
  20.66MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   6.34MB java.base                                            2.75MB byte[] for code metadata
   1.13MB svm.jar (Native Image)                               1.88MB byte[] for java.lang.String
 797.34kB benchmarks.jar                                       1.37MB java.lang.String
 114.46kB java.logging                                         1.10MB java.lang.Class
  56.80kB org.graalvm.nativeimage.base                       411.64kB com.oracle.svm.core.hub.DynamicHubCompanion
  50.59kB jdk.proxy1                                         349.93kB byte[] for reflection metadata
  48.91kB jdk.crypto.ec                                      304.69kB byte[] for general heap data
  48.74kB jdk.proxy3                                         278.11kB java.util.HashMap$Node
  25.57kB jdk.net                                            275.10kB java.lang.String[]
  24.47kB jdk.internal.reflect                               257.18kB java.lang.Object[]
  76.98kB for 8 more packages                                  2.46MB for 1184 more object types
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
------------------------------------------------------------------------------------------------------------------------
                        2.0s (7.7% of total time) in 202 GCs | Peak RSS: 1.13GB | CPU load: 6.33
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /Users/galder/1/fibula-show/2410-unfibula/unfibula/target/benchmarks (executable)
========================================================================================================================
Finished generating 'benchmarks' in 25.8s.

$ target/benchmarks
Exception in thread "main" java.lang.ExceptionInInitializerError
	at org.openjdk.jmh.runner.Runner.newBenchmarkParams(Runner.java:493)
	at org.openjdk.jmh.runner.Runner.getActionPlans(Runner.java:352)
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:543)
	at org.openjdk.jmh.runner.Runner.internalRun(Runner.java:309)
	at org.openjdk.jmh.runner.Runner.run(Runner.java:208)
	at org.openjdk.jmh.Main.main(Main.java:71)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
Caused by: java.lang.IllegalStateException: Consistency check failed for benchmark, off = 16, markerBegin = 12, markerEnd = 292
	at org.openjdk.jmh.util.Utils.check(Utils.java:375)
	at org.openjdk.jmh.util.Utils.check(Utils.java:365)
	at org.openjdk.jmh.infra.BenchmarkParams.<clinit>(BenchmarkParams.java:61)
	... 7 more
make: *** [Makefile:36: run] Error 1
```

The runtime error is now related to `BenchmarkParams`,
so apply the same pattern for `org.openjdk.jmh.infra.BenchmarkParamsL[0-4]`.
Then merge configuration, rebuild and run:

```shell
$ make merge-native-config

$ make

$ target/benchmarks
# JMH version: 1.37
# VM version: JDK 21.0.2, Substrate VM, 21.0.2+13
# *** WARNING: This VM is not supported by JMH. The produced benchmark data can be completely wrong.
# VM invoker: null/bin/java
# VM options: <none>
# Blackhole mode: full + dont-inline hint (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 5 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: org.sample.MyBenchmark.testMethod

WARNING: Not a HotSpot compiler command compatible VM ("Substrate VM-21.0.2"), compiler hints are disabled.
# Run progress: 0.00% complete, ETA 00:08:20
# Fork: 1 of 5
<failed to invoke the VM, caught IOException: Cannot run program "null/bin/java": error=2, No such file or directory>

# Run complete. Total time: 00:00:00

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark  Mode  Cnt  Score   Error  Units
```

The next error is:
`failed to invoke the VM, caught IOException: Cannot run program "null/bin/java": error=2, No such file or directory`.

### Experiment 005

Is the generated native image configuration deterministic?
That is, if neither the code nor the graalvm version changes,
does repeated invocations of the agent generate the same configuration?

Also, what happens with the merged configuration?

```shell
$ NATIVE_AGENT=true make run-jvm
$ make copy-native-config-agent-runner
$ make copy-native-config-agent-forked
$ make merge-native-config

$ git status
On branch main
nothing to commit, working tree clean
```

The worktree is clean, so the configurations seems to have been generated,
and the merged version also remained the same.

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
