# Http Header Validation

## Get native results

Build and get trace inlining information (fails with JDK 21 so use say JDK 23):

```shell
mvn package -DbuildArgs=-H:+TraceInlining
```

Then just run:
```shell
java -jar target/benchmarks.jar
```

## Get native results with profiling

Build it with debug info and symbols:

```shell
mvn package -Ddebug -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings
```

Run it with the DWARF profiler:

```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.bootstrap.DwarfPerfAsmProfiler:events=cycles:P
```
