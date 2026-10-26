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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

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
    static final MethodType GETTER_TYPE = methodType(Object.class, Object.class);
    volatile MethodHandle _accessor;

    @Setup
    public void setup() throws NoSuchFieldException
    {
        field = MyValue.class.getDeclaredField("age");
        value = new MyValue();
        ((MyValue) value).age = 42;
    }

    @Benchmark
    public int reflectInvokeGetField() throws IllegalAccessException
    {
        return (int) field.get(value);
    }

    @Benchmark
    public Object mhandleInvokeGetField() throws Throwable
    {
        // Replicate code as shown in latest Jackson 3.x:
        // https://github.com/FasterXML/jackson-databind/blob/4f153f09eb5b3970384678068ea2d97c3087755a/src/main/java/tools/jackson/databind/ser/BeanPropertyWriter.java
        return _accessorHandle().invokeExact(value);
    }

    private static class MyValue
    {
        public int age;
    }

    private MethodHandle _accessorHandle() throws IllegalAccessException {
        MethodHandle h = _accessor;
        if (h == null) {
            h = _unreflectAccessor();
            _accessor = h;
        }
        return h;
    }

    private MethodHandle _unreflectAccessor() throws IllegalAccessException {
        final MethodHandle h = MethodHandles.lookup().unreflectGetter(field);
        return h.asType(GETTER_TYPE);

//        MethodHandle h;
//        if (_member instanceof AnnotatedField) {
//            h = MethodHandles.lookup().unreflectGetter((Field) _member.getMember());
//        } else if (_member instanceof AnnotatedMethod method) {
//            h = MethodHandles.lookup().unreflect(method.getMember());
//        } else {
//            // 01-Dec-2014, tatu: Used to be illegal, but now explicitly allowed
//            // for virtual props
//            return null;
//        }
//        return h.asType(GETTER_TYPE);
    }
}
