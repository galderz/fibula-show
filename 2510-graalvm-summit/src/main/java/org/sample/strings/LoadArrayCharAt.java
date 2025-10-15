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

import static org.openjdk.jmh.annotations.CompilerControl.Mode.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class LoadArrayCharAt
{
    private String[] values;
    private int charAtIndex;

    @Setup
    public void setup()
    {
        values = new String[1];
        values[0] = "Latin1 string";
        charAtIndex = 3;
    }

    @Benchmark
    @CompilerControl(DONT_INLINE)
    public char latin1()
    {
        final String strLatin1 = values[0];
        return strLatin1.charAt(charAtIndex);
    }
}
