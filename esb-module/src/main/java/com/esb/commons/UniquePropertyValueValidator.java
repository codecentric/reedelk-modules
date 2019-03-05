package com.esb.commons;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

/**
 * Validates that all the items in the collection contain a property
 * value which is unique across all the elements in it.
 */
public class UniquePropertyValueValidator {

    private UniquePropertyValueValidator() {
    }

    public static <T, O> boolean validate(Collection<? extends T> flows, Function<T, O> extractPropertyValueFunction) {
        return flows.stream().map(extractPropertyValueFunction).allMatch(new HashSet<>()::add);
    }
}
