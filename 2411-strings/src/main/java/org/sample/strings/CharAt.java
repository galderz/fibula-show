package org.sample.strings;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class CharAt
{
    private String strLatin1;
    private String strUtf16;
    private int charAtIndex = 3;


    @Setup
    public void setup()
    {
        strLatin1 = "Latin1 string";
        strUtf16 = "UTF-\uFF11\uFF16 string";
    }

    @Benchmark
    //@CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public char charAtLatin1()
    {
        return strLatin1.charAt(charAtIndex);
    }

    @Benchmark
    //@CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public char charAtUtf16()
    {
        return strUtf16.charAt(charAtIndex);
    }
}
