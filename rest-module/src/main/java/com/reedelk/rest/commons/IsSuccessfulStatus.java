package com.reedelk.rest.commons;

public class IsSuccessfulStatus {

    public static boolean status(int code) {
        return ((200 <= code) && (code <= 299));
    }
}
