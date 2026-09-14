package org.sample.handles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static java.lang.invoke.MethodType.methodType;

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

