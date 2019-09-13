package com.reedelk.rest.commons;

import io.netty.handler.codec.http.HttpResponseStatus;

public class IsNotSuccessful {

    public static boolean status(HttpResponseStatus status) {
        return !(status == HttpResponseStatus.OK ||
                status == HttpResponseStatus.CREATED ||
                status == HttpResponseStatus.ACCEPTED ||
                status == HttpResponseStatus.NO_CONTENT ||
                status == HttpResponseStatus.MULTI_STATUS ||
                status == HttpResponseStatus.RESET_CONTENT ||
                status == HttpResponseStatus.PARTIAL_CONTENT ||
                status == HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION);
    }
}
