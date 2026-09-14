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

    class GetterHolder extends UnreflectHandleSupplier {
        Field field;

        public GetterHolder(Field field) {
            super(methodType(Object.class, Object.class));
            this.field = field;
        }

        @Override
        protected MethodHandle unreflect() throws IllegalAccessException {
            return MethodHandles.lookup().unreflectGetter(field);

//        if (_member instanceof AnnotatedField) {
//            return MethodHandles.lookup().unreflectGetter((Field) _member.getMember());
//        } else if (_member instanceof AnnotatedMethod method) {
//            return MethodHandles.lookup().unreflect(method.getMember());
//        } else {
//            // 01-Dec-2014, tatu: Used to be illegal, but now explicitly allowed
//            // for virtual props
//            return null;
//        }
        }
    }

    public static abstract class UnreflectHandleSupplier implements Supplier<MethodHandle> {
        private final MethodType asType;
        private volatile MethodHandle cachedHandle;

        public UnreflectHandleSupplier(MethodType asType) {
            this.asType = asType;
        }

        @Override
        public MethodHandle get() {
            MethodHandle h = cachedHandle;
            if (h == null) {
                h = initialize();
            }
            return h;
        }

        private synchronized MethodHandle initialize() {
            MethodHandle h = cachedHandle;
            if (h == null) {
                try {
                    h = postprocess(unreflect());
                } catch (IllegalAccessException e) {
                    throw sneakyThrow(e);
                }
                cachedHandle = h;
            }
            return h;
        }

        protected MethodHandle postprocess(MethodHandle mh) {
            if (mh == null) {
                return mh;
            }
            if (asType == null) {
                return mh.asFixedArity();
            }
            return mh.asType(asType);
        }

        protected abstract MethodHandle unreflect() throws IllegalAccessException;

        @Override
        public String toString() {
            return get().toString();
        }

        @SuppressWarnings("unchecked")
        public static <E extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws E {
            throw (E) throwable;
        }
    }
}
