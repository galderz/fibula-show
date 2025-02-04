# Missing JMH type serialization

Reproducer for serialization error caused by lack of registration of JMH output report type.

## Reproducer

Patch:
```diff
git diff
diff --git a/fibula-core/src/main/resources/META-INF/native-image/org.mendrugo.fibula/fibula-core/serialization-config.json b/fibula-core/src/main/resources/META-INF/native-image/org.mendrugo.fibula/fibula-core/>
index 7d7ac72..dc78991 100644
--- a/fibula-core/src/main/resources/META-INF/native-image/org.mendrugo.fibula/fibula-core/serialization-config.json
+++ b/fibula-core/src/main/resources/META-INF/native-image/org.mendrugo.fibula/fibula-core/serialization-config.json
@@ -165,9 +165,6 @@
     {
       "name":"org.openjdk.jmh.runner.ActionType"
     },
-    {
-      "name":"org.openjdk.jmh.runner.BenchmarkException"
-    },
     {
       "name":"org.openjdk.jmh.runner.IterationType"
     },
@@ -198,12 +195,6 @@
     {
       "name":"org.openjdk.jmh.runner.link.InfraFrame$Type"
     },
-    {
-      "name":"org.openjdk.jmh.runner.link.OutputFrame"
-    },
-    {
-      "name":"org.openjdk.jmh.runner.link.OutputFormatFrame"
-    },
     {
       "name":"org.openjdk.jmh.runner.link.ResultMetadataFrame"
     },
diff --git a/pom.xml b/pom.xml
index 367d18a..b11f055 100644
--- a/pom.xml
+++ b/pom.xml
@@ -24,6 +24,7 @@
         <maven.compiler.release>21</maven.compiler.release>

         <jmh.version>1.37</jmh.version>
+<!--        <jmh.version>1.38-SNAPSHOT</jmh.version>-->

         <compiler-plugin.version>3.12.1</compiler-plugin.version>
         <jar-plugin.version>3.4.2</jar-plugin.version>
```

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
# Benchmark: org.sample.ThrowKnownExceptionBenchmark.singleException

# Run progress: 0.00% complete, ETA 00:01:40
# Fork: 1 of 1
<forked VM failed with exit code 1>
<stdout last='20 lines'>
</stdout>
<stderr last='20 lines'>
</stderr>

Benchmark had encountered error, and fail on error was requested
ERROR: org.openjdk.jmh.runner.RunnerException: Benchmark caught the exception
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:572)
	at org.openjdk.jmh.runner.Runner.internalRun(Runner.java:309)
	at org.openjdk.jmh.runner.Runner.run(Runner.java:208)
	at org.mendrugo.fibula.MutiVmMain.main(MutiVmMain.java:61)
Caused by: org.openjdk.jmh.runner.BenchmarkException: Benchmark error
	at org.openjdk.jmh.runner.Runner.doFork(Runner.java:765)
	at org.openjdk.jmh.runner.Runner.runSeparate(Runner.java:657)
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:555)
	... 3 more
	Suppressed: java.lang.IllegalStateException: Forked VM failed with exit code 1
		... 6 more
```

## One Fix

Patch:
```diff
commit 1b105e0f1c2f20890bcc747c789de8f4dbf88799 (HEAD -> 1.38-SNAPSHOT-patches.v2, origin/1.38-SNAPSHOT-patches.v2, origin/1.37-patches.v2)
Author: Galder Zamarreño <galder@zamarreno.com>
Date:   Wed Aug 14 09:49:59 2024 +0200

    Print exception in case there's a subsequent failure reading the stream

diff --git a/jmh-core/src/main/java/org/openjdk/jmh/runner/ForkedMain.java b/jmh-core/src/main/java/org/openjdk/jmh/runner/ForkedMain.java
index c0d7cf6a..c0996663 100644
--- a/jmh-core/src/main/java/org/openjdk/jmh/runner/ForkedMain.java
+++ b/jmh-core/src/main/java/org/openjdk/jmh/runner/ForkedMain.java
@@ -87,6 +87,7 @@ class ForkedMain {

                 gracefullyFinished = true;
             } catch (Throwable ex) {
+                ex.printStackTrace(nakedErr);
                 exception = ex;
                 gracefullyFinished = false;
             } finally {
```

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
# Benchmark: org.sample.ThrowKnownExceptionBenchmark.singleException

# Run progress: 0.00% complete, ETA 00:01:40
# Fork: 1 of 1
com.oracle.svm.core.jdk.UnsupportedFeatureError: SerializationConstructorAccessor class not found for declaringClass: org.openjdk.jmh.runner.link.OutputFormatFrame (targetConstructorClass: java.lang.Object). Usually adding org.openjdk.jmh.runner.link.OutputFormatFrame to serialization-config.json fixes the problem.
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.util.VMError.unsupportedFeature(VMError.java:121)
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.reflect.serialize.SerializationSupport.getSerializationConstructorAccessor(SerializationSupport.java:163)
	at java.base@21.0.2/jdk.internal.reflect.MethodAccessorGenerator.generateSerializationConstructor(MethodAccessorGenerator.java:66)
	at java.base@21.0.2/jdk.internal.reflect.MethodAccessorGenerator.generateSerializationConstructor(MethodAccessorGenerator.java:54)
	at java.base@21.0.2/jdk.internal.reflect.ReflectionFactory.generateConstructor(ReflectionFactory.java:420)
	at java.base@21.0.2/jdk.internal.reflect.ReflectionFactory.newConstructorForSerialization(ReflectionFactory.java:412)
	at java.base@21.0.2/java.io.ObjectStreamClass.getSerializableConstructor(ObjectStreamClass.java:1445)
	at java.base@21.0.2/java.io.ObjectStreamClass$2.run(ObjectStreamClass.java:413)
	at java.base@21.0.2/java.io.ObjectStreamClass$2.run(ObjectStreamClass.java:385)
	at java.base@21.0.2/java.security.AccessController.executePrivileged(AccessController.java:129)
	at java.base@21.0.2/java.security.AccessController.doPrivileged(AccessController.java:319)
	at java.base@21.0.2/java.io.ObjectStreamClass.<init>(ObjectStreamClass.java:385)
	at java.base@21.0.2/java.io.ObjectStreamClass$Caches$1.computeValue(ObjectStreamClass.java:111)
	at java.base@21.0.2/java.io.ObjectStreamClass$Caches$1.computeValue(ObjectStreamClass.java:108)
	at java.base@21.0.2/java.io.ClassCache$1.computeValue(ClassCache.java:73)
	at java.base@21.0.2/java.io.ClassCache$1.computeValue(ClassCache.java:70)
	at java.base@21.0.2/java.lang.ClassValue.get(JavaLangSubstitutions.java:770)
	at java.base@21.0.2/java.io.ClassCache.get(ClassCache.java:84)
	at java.base@21.0.2/java.io.ObjectStreamClass.lookup(ObjectStreamClass.java:92)
	at java.base@21.0.2/java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1150)
	at java.base@21.0.2/java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:358)
	at org.openjdk.jmh.runner.link.BinaryLinkClient.pushFrame(BinaryLinkClient.java:125)
	at org.openjdk.jmh.runner.link.BinaryLinkClient.lambda$new$0(BinaryLinkClient.java:86)
	at jdk.proxy4/jdk.proxy4.$Proxy45.println(Unknown Source)
	at org.openjdk.jmh.runner.BaseRunner.doSingle(BaseRunner.java:146)
	at org.openjdk.jmh.runner.BaseRunner.runBenchmarksForked(BaseRunner.java:75)
	at org.openjdk.jmh.runner.ForkedRunner.run(ForkedRunner.java:72)
	at org.openjdk.jmh.runner.ForkedMain.main(ForkedMain.java:86)
	at java.base@21.0.2/java.lang.reflect.Method.invoke(Method.java:580)
	at org.mendrugo.fibula.NativeForkedMain.main(NativeForkedMain.java:21)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
Exception in thread "Thread-0" com.oracle.svm.core.jdk.UnsupportedFeatureError: SerializationConstructorAccessor class not found for declaringClass: org.openjdk.jmh.runner.link.OutputFormatFrame (targetConstructorClass: java.lang.Object). Usually adding org.openjdk.jmh.runner.link.OutputFormatFrame to serialization-config.json fixes the problem.
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.util.VMError.unsupportedFeature(VMError.java:121)
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.reflect.serialize.SerializationSupport.getSerializationConstructorAccessor(SerializationSupport.java:163)
	at java.base@21.0.2/jdk.internal.reflect.MethodAccessorGenerator.generateSerializationConstructor(MethodAccessorGenerator.java:66)
	at java.base@21.0.2/jdk.internal.reflect.MethodAccessorGenerator.generateSerializationConstructor(MethodAccessorGenerator.java:54)
	at java.base@21.0.2/jdk.internal.reflect.ReflectionFactory.generateConstructor(ReflectionFactory.java:420)
	at java.base@21.0.2/jdk.internal.reflect.ReflectionFactory.newConstructorForSerialization(ReflectionFactory.java:412)
	at java.base@21.0.2/java.io.ObjectStreamClass.getSerializableConstructor(ObjectStreamClass.java:1445)
	at java.base@21.0.2/java.io.ObjectStreamClass$2.run(ObjectStreamClass.java:413)
	at java.base@21.0.2/java.io.ObjectStreamClass$2.run(ObjectStreamClass.java:385)
	at java.base@21.0.2/java.security.AccessController.executePrivileged(AccessController.java:129)
	at java.base@21.0.2/java.security.AccessController.doPrivileged(AccessController.java:319)
	at java.base@21.0.2/java.io.ObjectStreamClass.<init>(ObjectStreamClass.java:385)
	at java.base@21.0.2/java.io.ObjectStreamClass$Caches$1.computeValue(ObjectStreamClass.java:111)
	at java.base@21.0.2/java.io.ObjectStreamClass$Caches$1.computeValue(ObjectStreamClass.java:108)
	at java.base@21.0.2/java.io.ClassCache$1.computeValue(ClassCache.java:73)
	at java.base@21.0.2/java.io.ClassCache$1.computeValue(ClassCache.java:70)
	at java.base@21.0.2/java.lang.ClassValue.get(JavaLangSubstitutions.java:770)
	at java.base@21.0.2/java.io.ClassCache.get(ClassCache.java:84)
	at java.base@21.0.2/java.io.ObjectStreamClass.lookup(ObjectStreamClass.java:92)
	at java.base@21.0.2/java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1150)
	at java.base@21.0.2/java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:358)
	at org.openjdk.jmh.runner.link.BinaryLinkClient.pushFrame(BinaryLinkClient.java:125)
	at org.openjdk.jmh.runner.link.BinaryLinkClient.lambda$new$0(BinaryLinkClient.java:86)
	at jdk.proxy4/jdk.proxy4.$Proxy45.println(Unknown Source)
	at org.openjdk.jmh.runner.ForkedMain.hangup(ForkedMain.java:124)
	at org.openjdk.jmh.runner.ForkedMain$HangupThread.run(ForkedMain.java:160)
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.thread.PlatformThreads.threadStartRoutine(PlatformThreads.java:833)
	at org.graalvm.nativeimage.builder/com.oracle.svm.core.posix.thread.PosixPlatformThreads.pthreadStartRoutine(PosixPlatformThreads.java:211)
<forked VM failed with exit code 1>
<stdout last='20 lines'>
</stdout>
<stderr last='20 lines'>
	at java.base@21.0.2/java.io.ObjectStreamClass.<init>(ObjectStreamClass.java:385)
	at java.base@21.0.2/java.io.ObjectStreamClass$Caches$1.computeValue(ObjectStreamClass.java:111)
	at java.base@21.0.2/java.io.ObjectStreamClass$Caches$1.computeValue(ObjectStreamClass.java:108)
	at java.base@21.0.2/java.io.ClassCache$1.computeValue(ClassCache.java:73)
	at java.base@21.0.2/java.io.ClassCache$1.computeValue(ClassCache.java:70)
	at java.base@21.0.2/java.lang.ClassValue.get(JavaLangSubstitutions.java:770)
	at java.base@21.0.2/java.io.ClassCache.get(ClassCache.java:84)
	at java.base@21.0.2/java.io.ObjectStreamClass.lookup(ObjectStreamClass.java:92)
	at java.base@21.0.2/java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1150)
	at java.base@21.0.2/java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:358)
	at org.openjdk.jmh.runner.link.BinaryLinkClient.pushFrame(BinaryLinkClient.java:125)
	at org.openjdk.jmh.runner.link.BinaryLinkClient.lambda$new$0(BinaryLinkClient.java:86)
	at jdk.proxy4/jdk.proxy4.$Proxy45.println(Unknown Source)
	at org.openjdk.jmh.runner.BaseRunner.doSingle(BaseRunner.java:146)
	at org.openjdk.jmh.runner.BaseRunner.runBenchmarksForked(BaseRunner.java:75)
	at org.openjdk.jmh.runner.ForkedRunner.run(ForkedRunner.java:72)
	at org.openjdk.jmh.runner.ForkedMain.main(ForkedMain.java:86)
	at java.base@21.0.2/java.lang.reflect.Method.invoke(Method.java:580)
	at org.mendrugo.fibula.NativeForkedMain.main(NativeForkedMain.java:21)
	at java.base@21.0.2/java.lang.invoke.LambdaForm$DMH/sa346b79c.invokeStaticInit(LambdaForm$DMH)
</stderr>

Benchmark had encountered error, and fail on error was requested
ERROR: org.openjdk.jmh.runner.RunnerException: Benchmark caught the exception
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:575)
	at org.openjdk.jmh.runner.Runner.internalRun(Runner.java:309)
	at org.openjdk.jmh.runner.Runner.run(Runner.java:208)
	at org.mendrugo.fibula.MutiVmMain.main(MutiVmMain.java:61)
Caused by: org.openjdk.jmh.runner.BenchmarkException: Benchmark error
	at org.openjdk.jmh.runner.Runner.doFork(Runner.java:768)
	at org.openjdk.jmh.runner.Runner.runSeparate(Runner.java:660)
	at org.openjdk.jmh.runner.Runner.runBenchmarks(Runner.java:558)
	... 3 more
	Suppressed: java.lang.IllegalStateException: Forked VM failed with exit code 1
		... 6 more
```