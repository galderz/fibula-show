package org.sample.handles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public abstract class UnreflectHandleSupplier implements Supplier<MethodHandle> {
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
