/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.sample.handles;

public abstract class InternalLoggerFactory {

    private static volatile InternalLoggerFactory defaultFactory;

    @SuppressWarnings("UnusedCatchParameter")
    private static InternalLoggerFactory newDefaultFactory(String name) {
//        InternalLoggerFactory f = useSlf4JLoggerFactory(name);
//        if (f != null) {
//            return f;
//        }

//        f = useLog4J2LoggerFactory(name);
//        if (f != null) {
//            return f;
//        }
//
//        f = useLog4JLoggerFactory(name);
//        if (f != null) {
//            return f;
//        }

        return useJdkLoggerFactory(name);
    }

//    private static InternalLoggerFactory useSlf4JLoggerFactory(String name) {
//        try {
//            InternalLoggerFactory f = Slf4JLoggerFactory.getInstanceWithNopCheck();
//            f.newInstance(name).debug("Using SLF4J as the default logging framework");
//            return f;
//        } catch (LinkageError ignore) {
//            return null;
//        } catch (Exception ignore) {
//            // We catch Exception and not ReflectiveOperationException as we still support java 6
//            return null;
//        }
//    }

//    private static InternalLoggerFactory useLog4J2LoggerFactory(String name) {
//        try {
//            InternalLoggerFactory f = Log4J2LoggerFactory.INSTANCE;
//            f.newInstance(name).debug("Using Log4J2 as the default logging framework");
//            return f;
//        } catch (LinkageError ignore) {
//            return null;
//        } catch (Exception ignore) {
//            // We catch Exception and not ReflectiveOperationException as we still support java 6
//            return null;
//        }
//    }

//    private static InternalLoggerFactory useLog4JLoggerFactory(String name) {
//        try {
//            InternalLoggerFactory f = Log4JLoggerFactory.INSTANCE;
//            f.newInstance(name).debug("Using Log4J as the default logging framework");
//            return f;
//        } catch (LinkageError ignore) {
//            return null;
//        } catch (Exception ignore) {
//            // We catch Exception and not ReflectiveOperationException as we still support java 6
//            return null;
//        }
//    }

    private static InternalLoggerFactory useJdkLoggerFactory(String name) {
        InternalLoggerFactory f = JdkLoggerFactory.INSTANCE;
        f.newInstance(name).debug("Using java.util.logging as the default logging framework");
        return f;
    }

    /**
     * Returns the default factory.  The initial default factory is
     * {@link JdkLoggerFactory}.
     */
    public static InternalLoggerFactory getDefaultFactory() {
        if (defaultFactory == null) {
            defaultFactory = newDefaultFactory(InternalLoggerFactory.class.getName());
        }
        return defaultFactory;
    }

    /**
     * Changes the default factory.
     */
    public static void setDefaultFactory(InternalLoggerFactory defaultFactory) {
        InternalLoggerFactory.defaultFactory = ObjectUtil.checkNotNull(defaultFactory, "defaultFactory");
    }

    /**
     * Creates a new logger instance with the name of the specified class.
     */
    public static InternalLogger getInstance(Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    /**
     * Creates a new logger instance with the specified name.
     */
    public static InternalLogger getInstance(String name) {
        return getDefaultFactory().newInstance(name);
    }

    /**
     * Creates a new logger instance with the specified name.
     */
    protected abstract InternalLogger newInstance(String name);

}
