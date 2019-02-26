package com.esb.foonnel.rest.commons;

public class Preconditions {

    public static void isNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

}
