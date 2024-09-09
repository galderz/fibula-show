package org.sample.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class BufferOneBenchmark
{
    private ByteBuf buffer;

    @Benchmark
    public long setGetLongLE()
    {
        return buffer.setLongLE(0, 1).getLongLE(0);
    }

    @Benchmark
    public ByteBuf setLongLE()
    {
        return buffer.setLongLE(0, 1);
    }

    @Setup
    public void setup()
    {
        buffer = new UnpooledUnsafeDirectByteBuf(
            UnpooledByteBufAllocator.DEFAULT
            , 64
            , 64
        );
        buffer.setIndex(0, 64);
    }

    @TearDown
    public void tearDown()
    {
        buffer.release();
    }
}