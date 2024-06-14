# Before

One shell:
```shell
benchmarking-linux
export JAVA_HOME=$HOME/1/jdk21u-dev/build/release-linux-x86_64/jdk
maven-java
cd jmh
mvn clean package -DskipTests
```

Another shell:
```shell
benchmarking-linux
graal-21
cd fibula
mvn clean package -DskipTests -Pnative -Dquarkus.package.jar.decompiler.enabled=true -Dquarkus.native.debug.enabled -Dfibula.native.additional-build-args=-H:-DeleteLocalSymbols
```

# Record

Press record!

# First JMH benchmark

Show JMH benchmark in IDE.

From the `jmh` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1
```

Note to audience: VM version, VM invoker

# First Fibula benchmark

Demonstrate that the benchmark is exactly the same as before in the IDE.

From the `fibula` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1
```

# Dig Deeper

Fibula works without any modifications to JMH.

How does it work underneath?
Enable DEBUG logging to learn more.

From the `fibula` folder:
```shell
java -Dquarkus.log.level=DEBUG ...
````

Focus:
```bash
2024-06-05 16:50:21,076 INFO  [io.quarkus] (main) fibula-benchmarks 999-SNAPSHOT on JVM (powered by Quarkus 3.11.0) started in 0.454s. Listening on: http://0.0.0.0:8080
```

`benchmarks.jar` is a Quarkus application running in jvm mode,
configured with a HTTP REST endpoint on port 8080.

This is a boostrap process that coordinates benchmark runs.

Focus:
```bash
2024-06-05 16:50:21,092 DEBUG [org.men.fib.boo.BenchmarkService] (main) Read from benchmark list file:
JMH S 27 org.sample.MyFirstBenchmark S 60 org.sample.jmh_generated.MyFirstBenchmark_helloWorld_jmhTest S 10 helloWorld S 10 Throughput E A 1 1 1 E E E E E E E E E E E E E E E E E
```

The bootstrap process located metadata related to the benchmark shown before.

More details about this when we discuss what happens at build time.

Focus:
```bash
2024-06-06 17:40:36,664 DEBUG [org.men.fib.boo.VmService] (main) Executing: target/fibula-1.0.0-SNAPSHOT-runner --command VM_INFO
```

The process that runs benchmarks is called a runner,
and it's a Quarkus command line application.

The vm information at the start is discovered via executing the runner application.

Focus:
```bash
2024-06-06 17:40:37,154 DEBUG [org.men.fib.boo.VmResource] (executor-thread-1) Received VM info: VmInfo[jdkVersion=21.0.2, vmName=Substrate VM, vmVersion=21.0.2+13]
```

How does the runner send back any information,
such as the vm info,
back to the bootstrap process?

It uses the HTTP rest client to communicate back to the bootstrap process.

The code uses Java Records and these are converted into JSON payloads. 

This log message shows the boostrap process receiving vm info via the HTTP REST endpoint.

Focus:
```bash
2024-06-06 17:40:37,281 DEBUG [org.men.fib.boo.BenchmarkService] (main) Executing: target/fibula-1.0.0-SNAPSHOT-runner --command FORK --supplier-name org_sample_jmh_generated_MyFirstBenchmark_helloWorld_jmhTest_helloWorld_Throughput_Supplier --params rO0ABXNy...
```

The boostrap process instructs the runner to run the first fork benchmark.

Again it invokes the runner as a command line application.

The parameters of the benchmark are Java serialized and sent as Base64 text.

Why do this?

We use JMH's types for representing benchmark parameters,
and these are serializable.

So, relying on serialization avoids the need for Fibula to reimplement encoding/decoding.

The runner, a native executable,
has been built with serialization config to understand how to deserialize the parameters.

Focus:
```bash
2024-06-06 17:40:47,591 DEBUG [org.men.fib.boo.IterationResource] (executor-thread-1) Received: IterationEnd[iteration=1, result=rO0AB...
```

The results of each iteration are sent back again via the HTTP REST endpoint.

This is the opposite scenario to benchmark parameters.

For the benchmark results we again rely on JMH types, which again are serializable,
and here the runner, a native executable,
java serializes the result and converts it to base 64 text,
and sends that as part of the JSON payload.

# Build Time

What exactly are these benchmark executing?

If we look at JMH, we see that it generates source code,
compiles it,
and then wraps it around code to capture metrics.
E.g. `./jmh/target/generated-sources/annotations/org/sample/jmh_generated/MyFirstBenchmark_helloWorld_jmhTest.java`

Fibula is doing something very similar to JMH,
but in a slightly different way.

It's executing bytecode that has been generated using a custom Quarkus extension,
derived from reading the annotations of the benchmarks.

The bytecode is generated just like any other Quarkus use cases.
For this use case, this can be found in
`fibula/target/decompiled/generated-bytecode/org/sample/jmh_generated/MyFirstBenchmark_helloWorld_jmhTest_helloWorld_Throughput_Function.java`.

# Why The Difference In Performance?

Use profiling to understand the differences in performance.

Start with the `perf` profiler
, which runs the forked execution via `perf stat`.

From the `jmh` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof perf
```

The `perf stat` output values might not very precise due to multiplexing.
Multiplexing happens when multiple counters are tracked with a single hardware counter.
To avoid this, let's limit track `perf` stats to `branches` and `instructions`:

From the `jmh` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof perf:events=branches,instructions,cycles
```

We have got some branch and instructions numbers.
Let's try to compare them with Fibula.

From the `fibula` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof perf:events=branches,instructions,cycles
```

JMH shows more branches and more instructions,
but the number of operations executed is higher.

It's hard to see the issue from just looking at this data.

To find more meaningful data,
normalize the counters to the number of operations executed.

JMH has the `perfnorm` profiler that does exactly that.

From the `jmh` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof perfnorm:events=branches,instructions,cycles
```

1 branch, 6 instructions per operation.

From the `fibula` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof perfnorm:events=branches,instructions,cycles
```

3 branches, 8 instructions per operation.

More branching and more instructions is the reason why SubstrateVM performs worse than HotSpot,
but what are these additional branches and instructions?

`perfasm` is an additional JMH profiler than help uncover this mistery:

From the `jmh` folder:
```shell
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof perfasm
```

1 branch and 6 instructions clearly visible in the output.

The branch is just the check of `isDone` to see if we have gone past the benchmark running time:
```bash
          ↗  0x00007f3c20b29e80:   movzbl		0x94(%r13), %r10d   ;*getfield isDone {reexecute=0 rethrow=0 return_oop=0}
          │                                                            ; - org.sample.jmh_generated.MyFirstBenchmark_helloWorld_jmhTest::helloWorld_thrpt_jmhStub@25 (line 123)
          │  0x00007f3c20b29e88:   movq		0x450(%r15), %r8
          │  0x00007f3c20b29e8f:   addq		$1, %r11            ; ImmutableOopMap {r9=Oop rbx=Oop r13=Oop }
          │                                                            ;*ifeq {reexecute=1 rethrow=0 return_oop=0}
          │                                                            ; - (reexecute) org.sample.jmh_generated.MyFirstBenchmark_helloWorld_jmhTest::helloWorld_thrpt_jmhStub@28 (line 123)
  33.64%  │  0x00007f3c20b29e93:   testl		%eax, (%r8)         ;   {poll}
  31.59%  │  0x00007f3c20b29e96:   testl		%r10d, %r10d
          ╰  0x00007f3c20b29e99:   je		0x7f3c20b29e80      ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                                       ; - org.sample.jmh_generated.MyFirstBenchmark_helloWorld_jmhTest::helloWorld_thrpt_jmhStub@28 (line 123)
```

Something similar can be achieved with Fibula,
but we need to make some minor command line and tooling changes:

* Instead of the default `perfasm`,
  use a profiler that invokes the `perf record` command just like `perfasm` does,
  but adds a DWARF callgraph.
  That is what the `org.mendrugo.fibula.bootstrap.DwarfPerfAsmProfiler` does.
* Switch from tracking `cycles` event to tracking `cycles:P`.
  The additional `:P` increases the precision of `perf record` by avoiding skidding problems.
* Skip the ASM part because there's no integration for that yet with Fibula.
* `perf annotate` will provide something like the `perfasm` output,
  but to be able to run it,
  instruct JMH to save the `perf.bin` file.

From the `fibula` folder:
```bash
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1 -prof org.mendrugo.fibula.bootstrap.DwarfPerfAsmProfiler:events=cycles:P\;skipAsm=true\;savePerfBin=true
```

Now run `perf annotate`:
```shell
perf annotate -i org.sample.MyFirstBenchmark.helloWorld-Throughput.perfbin
```

The `cmpb+jne` and the `jmp` at the end are the 2 branches for the `isDone` check:

```shell
 16.77 │90:┌──cmpb      $0x0,0xc(%rsi) 
       │   ├──jne       b0
       │   │  mov       %rax,%rcx
       │   │  inc       %rcx
 83.23 │   │  subl      $0x1,0x10(%r15)
       │   │↓ jle       d9
       │   │  mov       %rcx,%rax
       │   │↑ jmp       90
       │b0:└─→mov       %rax,0x20(%rsp)
```

```shell
 16.77 │90:┌─→cmpb      $0x0,0xc(%rsi)
       │   │↓ jne       b0
       │   │  mov       %rax,%rcx
       │   │  inc       %rcx
 83.23 │   │  subl      $0x1,0x10(%r15)
       │   │↓ jle       d9
       │   │  mov       %rcx,%rax
       │   └──jmp       90
```

The additional 3rd branch is the `subl+jle` for the safepoint checks:

```shell
 16.77 │90:   cmpb      $0x0,0xc(%rsi)
       │    ↓ jne       b0
       │      mov       %rax,%rcx
       │      inc       %rcx
 83.23 │   ┌──subl      $0x1,0x10(%r15)
       │   ├──jle       d9
       │   │  mov       %rcx,%rax
       │   │↑ jmp       90
       │b0:│  mov       %rax,0x20(%rsp)
       │   │↑ jmp       53
       │b7:│  mov       0x20(%rsp),%rax
       │   │  nop
       │   │→ call      _ZN36com.oracle.svm.core.thread.Safepoint27enterSlowPathSafepointCheckEJvv                    
       │   │  nop
       │   │↑ jmp       79
       │c8:│  mov       %r8,0x10(%rsp)
       │   │→ call      _ZN57com.oracle.svm.core.graal.snippets.StackOverflowCheckImpl26throwNewStackOverflowErrorEJvv
       │   │  nop
       │d3:│→ call      _ZN47com.oracle.svm.core.snippets.ImplicitExceptions28throwNewNullPointerExceptionEJvv
       │   │  nop
       │d9:└─→mov       0x10(%rsp),%r8
       │      xchg      %ax,%ax
       │    → call      _ZN36com.oracle.svm.core.thread.Safepoint27enterSlowPathSafepointCheckEJvv
```

This is a very contrived example,
but it gives an idea on what the type of assembly SubstrateVM generates compared to HotSpot.

In other examples I've run I've not seen differences between SubstrateVM and HotSpot,
so the noise we see here might not be so relevant.

In any case, some interesting observations can be made:

* SubstrateVM does dead-code-eliminate the empty method.
* But the safepoint check is not, should it also be dead-code-eliminated?
* Safepoint checks are not as fancy in SubstrateVM as in HotSpot,
  where instead of littering the code with branches,
  it [uses good/bad pages to avoid the branches](https://foojay.io/today/the-inner-workings-of-safepoints/).
* 1 conditional + 1 unconditional branch for the loop,
  while it could be done with just 1 branch.
* The increased branches, instructions and cycles per op shows the assembly in SubstrateVM is slower than HotSpot.
  But we can take a step further and emulate these assemblies side by side.
** [SubstrateVM uops.info](https://uica.uops.info/?code=loop%3A%0D%0Acmpb%20%20%20%20%20%20%240x0%2C0xc(%25rsi)%0D%0Ajne%20%20%20%20%20%20%20b0%0D%0Amov%20%20%20%20%20%20%20%25rax%2C%25rcx%0D%0Ainc%20%20%20%20%20%20%20%25rcx%0D%0Asubl%20%20%20%20%20%20%240x1%2C0x10(%25r15)%0D%0Ajle%20%20%20%20%20%20%20d9%0D%0Amov%20%20%20%20%20%20%20%25rcx%2C%25rax%0D%0Ajmp%20%20%20%20%20%20%20loop%0D%0A&syntax=asATT&uArchs=SNB&tools=uiCA&alignment=0&uiCAHtmlOptions=traceTable&uiCAHtmlOptions=dependencies)
** [HotSpot uops.info](https://uica.uops.info/?code=loop%3A%0D%0Amovzbl%09%090x94(%25r13)%2C%20%25r10d%0D%0Amovq%09%090x450(%25r15)%2C%20%25r8%0D%0Aaddq%09%09%241%2C%20%25r11%0D%0Atestl%09%09%25eax%2C%20(%25r8)%0D%0Atestl%09%09%25r10d%2C%20%25r10d%0D%0Aje%09%09loop%0D%0A&syntax=asATT&uArchs=SNB&tools=uiCA&alignment=0&uiCAHtmlOptions=traceTable&uiCAHtmlOptions=dependencies)

# Fibula Outings

Some real life examples using Fibula:

* Records equals/hashCode performance.
Initially performance was very bad when records support came out in SubstrateVM.
Christian improved the performance with this [PR](https://github.com/oracle/graal/pull/8109).
A JMH benchmark running with Fibula was able to confirm that the performance had improved significantly:

```shell
Benchmark                                  Mode  Cnt          Score         Error  Units
equalsPositions   GraalVM 24.0.0          thrpt    4  114064530.931 ±  303317.241  ops/s
hashcodePosition  GraalVM 24.0.0          thrpt    4  262827839.960 ± 4187526.946  ops/s
equalsPositions   GraalVM 23.1.0          thrpt    4     117738.738 ±    1891.331  ops/s
hashcodePosition  GraalVM 23.1.0          thrpt    4     292367.045 ±    6803.486  ops/s
```

* [Franz wanted to know if calling `Thread.isVirtual` via method handle instead of direct call would cause a regression in Substrate](https://github.com/quarkusio/quarkus/pull/39704/files#r1547368644).
Fibula showed that both approaches were as fast as each other,
assuming a constant method handle definition:

```shell
FibulaSample_07_IsVirtualMH.directCall        thrpt    4  1176840295.265 ± 46032071.001  ops/s
FibulaSample_07_IsVirtualMH.methodHandleCall  thrpt    4  1166157720.139 ± 59339014.156  ops/s
```

* A quick experiment building with `-H:+SourceLevelDebug` shows that it has a performance impact:

```shell
MyFirstBenchmark.helloWorld  -H:-SourceLevelDebug thrpt       1845624344.628          ops/s
MyFirstBenchmark.helloWorld  -H:+SourceLevelDebug thrpt       1640804740.323          ops/s
```

# Summary

Fibula allows you to run JMH benchmarks as GraalVM native executables.

# Origin

This is not my first time trying to get JMH to run with native executables.

I first experimented with it in 2020,
when I tried to hack JMH to make it work
and realised it relied on java serialization,
and at the time there was no support for it in native image.
and at the time there was no java serialization support.

A year or two later native image added java serialization support.
I tried to hack JMH once more and couldn't get it to work.
JMH, as is, requires a lot of configuration to make things work in native.
JMH has a client/server architecture written with plain Java NIO,
relies on java serialization...etc.

Last September I was on a train ride down to Ticino for a weekend away.
I had just written an email to Andrew Dinn about signing up to the HotSpot trainings,
saying that I wanted to understand how things worked underneath.
Then I thought how good was JMH to learn about how things worked...
Then I thought about Quarkus, how it could process annotations, generate bytecode... and produce native executables.
And then it clicked.

I didn't need to modify JMH to run JMH benchmarks as native executables.
Instead, I could use Quarkus to process JMH annotations,
generate bytecode just like JMH generates source code for the generated benchmarks,
generate native executables out of that bytecode and some glue code,
and voilá, I would have run JMH benchmarks as native executables.

# Fibula JVM mode

Fibula can also run in JVM mode.
This can be useful to detect any issues with Fibula itself:

```shell
export JAVA_HOME=$HOME/1/jdk21u-dev/build/release-linux-x86_64/jdk
maven-java
cd fibula-jvm
mvn clean package -DskipTests
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 1 -w 1
```

The results above are equal to JMH,
so the differences in performance cannot be atributted to generated bytecode shape.
