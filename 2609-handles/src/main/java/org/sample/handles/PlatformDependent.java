package org.sample.handles;

import java.lang.invoke.VarHandle;

public final class PlatformDependent {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PlatformDependent.class);
    private static final boolean VAR_HANDLE;

    static {
        VAR_HANDLE = initializeVarHandle();
    }

    private static boolean initializeVarHandle() {
        if (isUnaligned() || javaVersion() < 9 ||
            PlatformDependent0.isNativeImage()) {
            return false;
        }
        boolean varHandleAvailable = false;
        Throwable varHandleFailure;
        try {
            VarHandle.storeStoreFence();
            varHandleAvailable = VarHandleFactory.isSupported();
            varHandleFailure = VarHandleFactory.unavailableCause();
        } catch (Throwable t) {
            // no-op
            varHandleFailure = t;
        }
        if (varHandleFailure != null) {
            logger.debug("java.lang.invoke.VarHandle: unavailable, reason: {}", varHandleFailure.toString());
        } else {
            logger.debug("java.lang.invoke.VarHandle: available");
        }
        boolean varHandleEnabled = varHandleAvailable &&
            SystemPropertyUtil.getBoolean("io.netty.varHandle.enabled", varHandleAvailable);
        if (logger.isTraceEnabled() && varHandleFailure != null) {
            logger.debug("-Dio.netty.varHandle.enabled: {}", varHandleEnabled, varHandleFailure);
        } else if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.varHandle.enabled: {}", varHandleEnabled);
        }
        return varHandleEnabled;
    }

    public static boolean isUnaligned() {
        return PlatformDependent0.isUnaligned();
    }

    public static int javaVersion() {
        return PlatformDependent0.javaVersion();
    }

    public static VarHandle longLeArrayView() {
        if (VAR_HANDLE) {
            return VarHandleFactory.longLeArrayView();
        }
        return null;
    }

}
