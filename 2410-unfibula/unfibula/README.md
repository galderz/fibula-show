# Unfibula

## Research Progress

### Experiment 001

What happens if you take a stock build `benchmarks.jar` and try to produce a native image out of it?

```shell
$ native-image -jar target/benchmarks.jar
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
