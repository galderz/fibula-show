todo: adjust running times to be long enough to see differences

RECORD!!!

# Before

One shell:
maven-21
cd jmh
mvn clean package -DskipTests

Another shell:
graal-21
cd fibula
mvn clean package -DskipTests -Pnative -Dquarkus.package.decompiler.enabled=true
^ todo consider building it already with debug info et al

# First JMH benchmark

jmh folder:
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 10 -w 10

Note: VM version, VM invoker

# First Fibula benchmark

fibula folder:
java -jar target/benchmarks.jar MyFirst -f 1 -i 1 -wi 1 -r 10 -w 10

# Dig Deeper

Fibula works without any modifications to JMH.
How does it work underneath?
Enable DEBUG logging to learn more.

fibula folder:
java -Dquarkus.log.level=DEBUG ...

Focus:
2024-06-05 16:50:21,076 INFO  [io.quarkus] (main) fibula-benchmarks 999-SNAPSHOT on JVM (powered by Quarkus 3.11.0) started in 0.454s. Listening on: http://0.0.0.0:8080

benchmarks.jar is a quarkus application running in jvm mode,
configured with a HTTP REST endpoint on port 8080.
This is a boostrap process that coordinates benchmark runs.

Focus:
2024-06-05 16:50:21,092 DEBUG [org.men.fib.boo.BenchmarkService] (main) Read from benchmark list file:
JMH S 27 org.sample.MyFirstBenchmark S 60 org.sample.jmh_generated.MyFirstBenchmark_helloWorld_jmhTest S 10 helloWorld S 10 Throughput E A 1 1 1 E E E E E E E E E E E E E E E E E

The bootstrap process located metadata related to the benchmark shown before.
More details about this when we discuss what happens at build time.

Focus:
2024-06-06 17:40:36,664 DEBUG [org.men.fib.boo.VmService] (main) Executing: target/fibula-1.0.0-SNAPSHOT-runner --command VM_INFO

The process that runs benchmarks is called a runner,
and it's a Quarkus command line application.
The vm information at the start is discovered via executing the runner application.

Focus:
2024-06-06 17:40:37,154 DEBUG [org.men.fib.boo.VmResource] (executor-thread-1) Received VM info: VmInfo[jdkVersion=21.0.2, vmName=Substrate VM, vmVersion=21.0.2+13]

How does the runner send back any information,
such as the vm info,
back to the bootstrap process?
It uses the HTTP rest client to communicate back to the bootstrap process.

The code uses java records and these are converted into JSON payloads. 

This log message shows the boostrap process receiving vm info via the HTTP REST endpoint.

Focus:
2024-06-06 17:40:37,281 DEBUG [org.men.fib.boo.BenchmarkService] (main) Executing: target/fibula-1.0.0-SNAPSHOT-runner --command FORK --supplier-name org_sample_jmh_generated_MyFirstBenchmark_helloWorld_jmhTest_helloWorld_Throughput_Supplier --params rO0ABXNy

The boostrap process instructs the runner to run the first fork benchmark.
Again it invokes the runner as a command line application.

The parameters of the benchmark are java serialized and sent as base 64 text.
Why do this?
We use JMH's types for representing benchmark parameters,
and these are serializable.
So, relying on serialization avoids the need for Fibula to reimplement encoding/decoding.

The runner, a native executable,
has been built with serialization config to understand how to deserialize the parameters.

Focus:
2024-06-06 17:40:47,591 DEBUG [org.men.fib.boo.IterationResource] (executor-thread-1) Received: IterationEnd[iteration=1, result=rO0AB...

The results of each iteration are sent back again via the HTTP REST endpoint.

This is the opposite scenario to benchmark parameters.
For the benchmark results we again rely on JMH types, which again are serializable,
and here the runner, a native executable,
java serializes the result and converts it to base 64 text,
and sends that as part of the JSON payload.

# Build Time

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
And then it clicked!
I didn't need to modify JMH to run JMH benchmarks as native executables!
Instead, I could use Quarkus to process JMH annotations,
generate bytecode just like JMH generates source code for the generated benchmarks,
generate native executables out of that bytecode and some glue code,
and voilá, I would have run JMH benchmarks as native executables.

We can instruct Fibula 


...

In short:
Fibula is a combination of two quarkus microservices that enable JMH benchmarks to execute when running as GraalVM native executables,
reusing as much as JMH API as possible.
