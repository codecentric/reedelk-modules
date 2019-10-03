package com.reedelk.esb.commons;

public class IsSourceAssignableToTarget {

    public static boolean from(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.isAssignableFrom(targetClazz);
    }
}
