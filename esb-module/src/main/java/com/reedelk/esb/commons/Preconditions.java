package com.reedelk.esb.commons;

import java.util.Optional;
import java.util.stream.Stream;

public class Preconditions {

    private Preconditions() {
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static void checkState(boolean expression, String errorMessage, Object... args) {
        if (!expression) {
            throw new IllegalStateException(String.format(errorMessage, args));
        }
    }

    public static void checkNotNull(Object obj, String errorMessage) {
        if (obj == null) {
            throw new NullPointerException(errorMessage);
        }
    }

    public static <T> T checkAtLeastOneAndGetOrThrow(Stream<T> stream, String errorMessage, Object... args) {
        return stream.findFirst().orElseThrow(() -> new IllegalStateException(String.format(errorMessage, args)));
    }

    public static <T> T checkIsPresentAndGetOrThrow(Optional<T> optional, String errorMessage, Object... args) {
        return optional.orElseThrow(() -> new IllegalStateException(String.format(errorMessage, args)));
    }
}
