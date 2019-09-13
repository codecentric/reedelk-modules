package com.reedelk.rest.client;

import com.reedelk.rest.client.strategy.ResponseHandler;

public class ResponseHandlerProvider {
    public static ResponseHandler<byte[]> from(HttpResponseWrapper responseData) {
        return (response, byteBuff) -> {
            responseData.status(response.status());
            responseData.headers(response.responseHeaders());
            return byteBuff.asByteArray();
        };
    }
}
