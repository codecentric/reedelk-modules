package com.reedelk.rest.client;

import com.reedelk.rest.client.strategy.ResponseHandler;

public class ResponseHandlerProvider {
    public static ResponseHandler<byte[]> from(ClientResponseData responseData) {
        return (response, byteBuff) -> {
            responseData.setStatus(response.status());
            responseData.setHeaders(response.responseHeaders());
            return byteBuff.asByteArray();
        };
    }
}
