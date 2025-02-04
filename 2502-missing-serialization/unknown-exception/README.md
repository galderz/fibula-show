# Missing exception serialization

Reproducer when the exception thrown has not been registered for serialization.

Output:
```bash
# JMH version: fibula:999-SNAPSHOT
# VM version: JDK 21.0.2, Substrate VM, GraalVM CE 21.0.2+13.1
# *** WARNING: This VM is not supported by JMH. The produced benchmark data can be completely wrong.
# VM invoker: target/benchmarks
# VM options: <none>
# Compiler hints: disabled (Forced off)
# Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
# Warmup: 5 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time
# Benchmark: org.sample.ThrowKnownExceptionBenchmark.withoutStateException

# Run progress: 50.00% complete, ETA 00:00:00
# Fork: 1 of 1
# Warmup Iteration   1: <failure>

java.lang.ArrayStoreException: Testing throwing an ArrayStoreException
	at org.sample.ThrowKnownExceptionBenchmark.withoutStateException(ThrowKnownExceptionBenchmark.java:19)
	at org.sample.jmh_generated.ThrowKnownExceptionBenchmark_withoutStateException_jmhTest.withoutStateException_thrpt_jmhStub(ThrowKnownExceptionBenchmark_withoutStateException_jmhTest.java:121)
	at org.sample.jmh_generated.ThrowKnownExceptionBenchmark_withoutStateException_jmhTest.withoutStateException_Throughput(ThrowKnownExceptionBenchmark_withoutStateException_jmhTest.java:84)
	at java.base@21.0.2/java.lang.reflect.Method.invoke(Method.java:580)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:527)
	at org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call(BenchmarkHandler.java:504)
	at java.base@21.0.2/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	at java.base@21.0.2/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
	at java.base@21.0.2/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	at java.base@21.0.2/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	at java.base@21.0.2/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	at java.base@21.0.2/java.lang.Thread.runWith(Thread.java:1596)
	at java.base@21.0.2/java.lang.Thread.run(Thread.java:1583)
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.thread.PlatformThreads.threadStartRoutine(PlatformThreads.java:833)
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.posix.thread.PosixPlatformThreads.pthreadStartRoutine(PosixPlatformThreads.java:211)




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