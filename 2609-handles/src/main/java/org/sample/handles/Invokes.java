package org.sample.handles;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class Invokes
{
    Object value;
    Field field;

    // Method Handles
    GetterHolder accessor;

    @Setup
    public void setup() throws NoSuchFieldException
    {
        field = MyValue.class.getDeclaredField("age");
        value = new MyValue();
        ((MyValue) value).age = 42;

        accessor = new GetterHolder(field);
    }

    @Benchmark
    public int reflectInvokeGetField() throws IllegalAccessException
    {
        return (int) field.get(value);
    }

    @Benchmark
    public Object mhandleInvokeGetField() throws Throwable
    {
        return accessor.get().invokeExact(value);
    }

    private static class MyValue
    {
        public int age;
    }
}
