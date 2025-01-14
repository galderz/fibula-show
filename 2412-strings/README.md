# Http Header Validation

## Get native results

Build:

```shell
mvn package
```

Then just run:
```shell
java -jar target/benchmarks.jar
```

You can get trace inlining information (fails with GraalVM for DK 21 so use say GraalVM for JDK 23):

```shell
mvn package -DbuildArgs=-H:+TraceInlining
```

You can build for jvm mode with:

```shell
mvn package -Djvm.mode
```

## Get native results with profiling

Build it with debug info and symbols:

```shell
mvn package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings
```

Run it with the DWARF profiler:

```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.DwarfPerfProfiler:events=cycles:P
```

# Debugging the compiler

Execute with:

```shell
mvn package -DbuildArgs=--debug-attach="*:8000"
```

## Increasing node count allowance to inline methods

```shell
mvn package -DbuildArgs=-H:MaxNodesInTrivialMethod=40
```
