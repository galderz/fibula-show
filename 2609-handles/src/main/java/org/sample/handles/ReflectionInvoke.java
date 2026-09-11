package org.sample.handles;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
public class ReflectionInvoke
{
    MyValue value;
    Field field;

    @Setup
    public void setup() throws NoSuchFieldException
    {
        field = MyValue.class.getDeclaredField("name");
        value = new MyValue();
        value.name = "my-name";
    }

    @Benchmark
    public String getField() throws IllegalAccessException
    {
        return (String) field.get(value);
    }

    private static class MyValue
    {
        public String name;
    }
}
