package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.ESBException;

import java.util.function.Predicate;

public class Preconditions {

    public static <Type> void requireTrue(Predicate<Type> predicate, Type type, String errorMessage) {
        if (!predicate.test(type)) {
            throw new ESBException(errorMessage);
        }
    }

    public static <Type> Type requireNotNull(Type object, String errorMessage) {
        if (object == null) {
            throw new ESBException(errorMessage);
        }
        return object;
    }

    public static String requireNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ESBException(message);
        }
        return value;
    }
}
