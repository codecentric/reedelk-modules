package com.reedelk.rest.commons;

import io.netty.handler.codec.http.HttpResponseStatus;

public class IsRedirectionStatus {

    public static boolean status(HttpResponseStatus status) {
        int statusCode = status.code();
        return ((300 <= statusCode) && (statusCode <= 399));
    }
}
