# Http Header Validation

## Get native results

Build and get trace inlining information (fails with JDK 21 so use say JDK 23):

```shell
mvn package -Dnative -Dquarkus.native.additional-build-args=-H:+TraceInlining
```

Then just run:
```shell
java -jar target/benchmarks.jar
```

## Get native results with profiling

Build it with debug info and symbols:

```shell
mvn package -Dnative -Dquarkus.native.debug.enabled -Dquarkus.native.additional-build-args=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings
```

Run it with the DWARF profiler:

```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.bootstrap.DwarfPerfAsmProfiler:events=cycles:P
```

# Debugging the compiler

Build native with options to log optimizations:

```shell
mvn package -DbuildArgs=-H:+TrackNodeSourcePosition,-H:OptimizationLog=Directory,-H:OptimizationLogPath=$PWD/target/optimization_log
```

Checkout GraalVM source code, change directory to `substratevm` and from there execute:

```shell
mx profdiff  --inliner-reasoning true report $PATH_TO/target/optimization_log
```

You can also step through the compiler with an IDE by making it run single-threaded:

```shell
mvn package -DbuildArgs=--debug-attach="*:8000",-Djava.util.concurrent.ForkJoinPool.common.parallelism=1
```
