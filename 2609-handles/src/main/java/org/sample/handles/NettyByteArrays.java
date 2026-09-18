package org.sample.handles;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class NettyByteArrays
{
    @Param({"256"})
    private int size;

    byte[] b;

    int index;

    @Setup
    public void setup()
    {
        final Random r = new Random(42);

        b = new byte[size];
        for (int i = 0; i < b.length; i++)
        {
            b[i] = (byte) r.nextInt();
        }

        index = 0;
    }

    /**
     * Mimic Netty's VarHandleByteBufferAccess.getLongLE()
     */
    @Benchmark
    public long vhandleGetLongLE()
    {
        return (long) PlatformDependent.longLeArrayView().get(b, index);
    }

    @Benchmark
    public long plainGetLongLE()
    {
        return ((long) b[index] & 0xFF) |
            ((long) b[index + 1] & 0xFF) << 8 |
            ((long) b[index + 2] & 0xFF) << 16 |
            ((long) b[index + 3] & 0xFF) << 24 |
            ((long) b[index + 4] & 0xFF) << 32 |
            ((long) b[index + 5] & 0xFF) << 40 |
            ((long) b[index + 6] & 0xFF) << 48 |
            ((long) b[index + 7] & 0xFF) << 56;
    }
}
