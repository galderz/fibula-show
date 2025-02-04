package org.sample;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.BenchmarkException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.DuplicateFormatFlagsException;

@State(Scope.Thread)
@Fork(1)
public class ThrowKnownExceptionBenchmark
{
    @Benchmark
    public void withoutStateException()
    {
        throw new ArrayStoreException("Testing throwing an ArrayStoreException");
    }

    @Benchmark
    public void withStateException()
    {
        throw new DuplicateFormatFlagsException("MyFlag");
    }
}
