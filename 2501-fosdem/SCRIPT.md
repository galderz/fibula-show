# FOSDEM 2025 Script

# First Run

`String.charAt` JMH benchmark:
```java
@BenchmarkMode(Mode.AverageTime)
// ...
class CharAt {
    String[] values;
    int charAtIndex;

    @Setup
    void setup() {
        values = new String[2];
        values[0] = "Latin1 string";
        values[1] = "UTF-\uFF11\uFF16 string";
        charAtIndex = 3;
    }

    @Benchmark
    char charAtLatin1() {
        return values[0].charAt(charAtIndex);
    }

    @Benchmark
    char charAtUtf16() {
        return values[1].charAt(charAtIndex);
    }
}
```

Why this benchmark? E.g. web frameworks http header validation.

For reference, this is what `String.charAt` implementation looks like and related code:
```java
class String
{
    static final byte LATIN1 = 0;
    final byte[] value;
    final byte coder;

    char charAt(int index)
    {
        if (isLatin1())
        {
            return StringLatin1.charAt(value, index);
        }
        else
        {
            return StringUTF16.charAt(value, index);
        }
    }

    boolean isLatin1()
    {
        return /* ... */ && coder == LATIN1;
    }
}
```

`StringLatin1.charAt`:
```java
class StringLatin1
{
    static char charAt(byte[] value, int index)
    {
        checkIndex(index, value.length);
        return (char)(value[index] & 0xff);
    }
}
```

Generated JMH source code:
```java
class CharAt_charAtLatin1_jmhTest
{
    public static void charAtLatin1_avgt_jmhStub(
        InfraControl control
        , RawResults result
        , Blackhole blackhole
        , CharAt_jmhType l_charat0_0
    ) throws Throwable
    {
        long operations = 0;
        long realTime = 0;
        result.startTime = System.nanoTime();
        do
        {
            blackhole.consume(l_charat0_0.charAtLatin1());
            operations++;
        }
        while(!control.isDone);
        result.stopTime = System.nanoTime();
        result.realTime = realTime;
        result.measuredOps = operations;
    }
}
```

Run the benchmark:
```shell
java -jar target/benchmarks.jar
```

Question: Is this fast or is it slow?

Question: Considerable difference between latin and utf16 numbers, why is it that?

Another question: who am I?

# First Profile

Benchmark built with DWARF debug info and some additional arguments to increase profiling precision.

Run the benchmark with a custom profiler that wraps the native image invocation with `perf record` with DWARF call graphs:
```shell
java -jar target/benchmarks.jar -prof org.mendrugo.fibula.PerfDwarfProfiler:events=cycles:pp
```

Inspect the perf files using `perf annotate`, e.g.
```shell
perf annotate -i org.sample.strings.CharAt.charAtLatin1-AverageTime.perfbin
```

* `StringLatin1.charAt` is the hottest method.
* `String.charAt` calls `StringLatin1.charAt`.

How does this differ to with the utf16 version?

```shell
perf annotate -i org.sample.strings.CharAt.charAtUtf16-AverageTime.perfbin
```

* `String.charAt` is the hottest method.
* No calls to `StringUTF16.charAt` because it has been inlined.

Theory: utf16 is faster than latin1 because it inlines more of the hot code.

How can we prove this theory?

# Trivial Run

Native image inlines methods that it considers trivial.
Methods made of 20 compiler nodes or less are considered trivial.
This number is configurable.
Rebuild the native image doubling that number, enabling bigger methods to be inlined:

```shell
mvn package -DbuildArgs=-H:MaxNodesInTrivialMethod=40
```

Run the benchmark and see if we see any differences:
```shell
java -jar target/benchmarks.jar
```

# Trivial Profile

The performance is better and the difference between latin1 and utf16 is gone.
How can we verify that indeed the inlining improvements really happened?
We can profile it just like we did before.

```shell
perf annotate -i org.sample.strings.CharAt.charAtLatin1-AverageTime.perfbin
```

We now know at least one reason why utf16 was faster than latin1.
There could be more but that is beyond this presentation.

The other question remains:
Are these numbers, even with expanded inlining, fast or slow?

# HotSpot

We can compare those numbers with the HotSpot JIT:

```shell
mvn package -Djvm.mode
```

Run with perfasm:

```shell
java -jar target/benchmarks.jar -prof perfasm charAtLatin1
```

JIT achieves better numbers,
but how does it achieve those?

More aggressive inlining.
This is something JITs can do because they know what is hot vs AOT.

What about PGO?
Could PGO be as fast as JIT?

# PGO Run

Let's try it out:

```shell
java -jar target/benchmarks.jar charAtLatin1
```

Starts running with a PGO instrumented binary.
It injects a warmup fork with the instrumented binary.
When that completes,
it takes the profiling information gathered during the warmup fork,
and rebuilds the binary to obtain a PGO optimized native image.

It obtains ~20% better results than JIT.
How does it achieve that?

# PGO Profile

Look at the profiling data:
```shell
perf annotate -i org.sample.strings.CharAt.charAtLatin1-AverageTime.perfbin
```

Loop is unrolled, 8 times.
`String.coder` check to decide on latin1 or utf16 remains.

# Pre-talk

New tab and rename:
```shell
cd $HOME/1/fosdem.present/first-run && graal-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/first-profile && graal-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/trivial-run && graal-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/trivial-profile && graal-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/hotspot && maven-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/pgo-run && ee-graal-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/pgo-profile && ee-graal-21 && ./prepare.sh && clear
```

New tab and rename:
```shell
cd $HOME/1/fosdem.present/pgo-profile && clear
```
