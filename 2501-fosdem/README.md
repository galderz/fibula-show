# FOSDEM 2025

# First Run

Show and explain `CharAt.java`

Build benchmark:
```shell
cd first-run && graal-21
mvn package
```

Run benchmark:
```shell
java -jar target/benchmarks.jar
```

Question: Is this fast or is it slow?

Question: Considerable difference between latin and utf16 numbers, why is it that?

# First Profile

Explain command line arguments and output while this is running (to save time):
* `-Ddebug=true` instructs the native image process to generate DWARF debug info.
It has no impact on how the native image is compiled and does not affect how fast it runs.
* `-H:-DeleteLocalSymbols` keeps the symbols in the binary, improves readability of perf output.
* `-H:+SourceLevelDebug` enables full parameter and local variable information.
  Affects how compilation is done and can slow down execution.
* `-H:+TrackNodeSourcePosition` tracks bytecode position of compiler nodes.
Affects how compilation is done and can slow down execution.
* `-H:+DebugCodeInfoUseSourceMappings` forces using source mappings in debug info.
Affects how compilation is done and can slow down execution.
```shell
cd first-profile && graal-21
mvn package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings
```

Use a custom profiler wrapping the native invocation around a `perf record` call with DWARF call graph.
When the benchmark concludes a separate perf file will be generated.
```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles:pp
```

Inspect the perf files using `perf annotate`, e.g.
```shell
perf annotate -i org.sample.strings.CharAt.charAtLatin1-AverageTime.perfbin
```

* `StringLatin1.charAt` displayed which means it didn't get inlined into `String.charAt` (show source code).
* Show the JMH generated source code, that calls into `CharAt.charAtLatin1`.
* Show `String.charAt` that shows invoking `StringLatin1.charAt`.
[todo any other observations]

How does this differ to with the utf16 version?
```shell
perf annotate -i org.sample.strings.CharAt.charAtUtf16-AverageTime.perfbin
```

`String.charAt` displayed right away with no signs o calls to `StringUTF16.charAt`.
The method has been inlined into `String.charAt`.

This is a big difference compared to how latin1 worked.
Could this difference in inlining be the reason for the performance difference?

# Trivial Run

We can verify that by tweaking inlining parameters used to build the native image.
One of the situations where methods are inlined is when they are trivial.
`MaxNodesInTrivialMethod` sets an upper bound on the number of nodes that a trivial method can contain.
The default value is 20.
What happens when we double that, say to 40?

```shell
cd trivial-run
mvn package -DbuildArgs=-H:MaxNodesInTrivialMethod=40
```

Now run the benchmark:

```shell
java -jar target/benchmarks.jar
```

# Trivial Profile

The performance is better and the difference between latin1 and utf16 is gone.
How can we verify that indeed the inlining improvements really happened?
We can profile it just like we did before

```shell
cd trivial-profile
mvn package -Ddebug=true -DbuildArgs=-H:-DeleteLocalSymbols,-H:+SourceLevelDebug,-H:+TrackNodeSourcePosition,-H:+DebugCodeInfoUseSourceMappings,-H:MaxNodesInTrivialMethod=40
```

`StringLatin1.charAt` has been inlined into `String.charAt`,
performance increased and we don't see any more big differences between latin1 and utf16.

Going back to our other question:
Are these numbers we get with native image slow or fast?

# HotSpot Run

We can build the same project to run with JVM mode:
```shell
cd hotspot
maven-11
mvn package -Djvm.mode
```

Now run the benchmark:
```shell
java -jar target/benchmarks.jar
```

Results are better than native image.
Where are the improvements coming from?

# HotSpot Perfasm

Run the benchmark with the `perfasm` profiler:
```shell
java -jar target/benchmarks.jar -prof perfasm
```

* `String.chartAt` gets inlined into the JMH generated code.
* [todo any other observations]

# PGO Run

One more thing...
Can native image be as fast as hotspot?
What if we use PGO?

```shell
cd pgo-run
ee-graal-21
mvn package -Dpgo
```

Then run only the `charAtLatin1` benchmark:
```shell
java -jar target/benchmarks.jar charAtLatin1
```

# PGO Profile

Observe that the performance with PGO is roughly same as with hotspot.
But why does this happen?

We can apply the same profiling that we did before, first build it:
```shell
cd pgo-profile
ee-graal-21
mvn package -Dpgo.perf
```

Then run it:
```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles:pp charAtLatin1
```

Finally inspect the perf binary file:
```shell
perf annotate -i org.sample.strings.CharAt.charAtLatin1-AverageTime.perfbin
```

Observations:
* [todo]

# What is skidding?

https://easyperf.net/blog/2018/06/08/Advanced-profiling-topics-PEBS-and-LBR
https://easyperf.net/blog/2018/08/29/Understanding-performance-events-skid
