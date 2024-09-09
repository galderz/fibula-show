package org.sample.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;
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
public class BufferTwoBenchmark
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
        try
        {
            System.out.printf("Load PooledDirectByteBuf class: %s%n", Class.forName("io.netty.buffer.PooledDirectByteBuf"));
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("io.netty.buffer.PooledDirectByteBuf class not found");
        }

        System.out.printf("Has unsafe: %b%n", PlatformDependent.hasUnsafe());
        System.out.printf("Prefer direct buffer: %b%n", PlatformDependent.directBufferPreferred());
        buffer = PooledByteBufAllocator.DEFAULT.buffer(8, 8);
    }

    @TearDown
    public void tearDown()
    {
        buffer.release();
    }
}
