package com.reedelk.rest.commons;

import com.reedelk.runtime.api.exception.ESBException;

import java.util.function.Predicate;

public class Preconditions {

    public static <T> void requireTrue(Predicate<T> predicate, T input, String errorMessage) {
        if (!predicate.test(input)) {
            throw new ESBException(errorMessage);
        }
    }

    public static <T> T requireNotNull(T object, String errorMessage) {
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
