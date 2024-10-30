# Unfibula

## Research Progress

### Experiment 002

Add flag to avoid fallback:

```shell
$ native-image --no-fallback -jar benchmarks.jar
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
