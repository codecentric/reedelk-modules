package com.reedelk.rest.server.mapper;

import io.netty.handler.codec.http.HttpHeaders;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Map;
import java.util.Optional;

class AdditionalHeader {

    /**
     * For each additional header, if present in the current headers it gets replaced,
     * otherwise it is  added to the current headers collection.
     * @param response the current http response object.
     * @param additionalHeaders additional user defined headers.
     */
    static void addAll(HttpServerResponse response, Map<String, String> additionalHeaders) {
        HttpHeaders currentHeaders = response.responseHeaders();
        additionalHeaders.forEach((headerName, headerValue) -> {
            Optional<String> optionalMatchingHeaderName = matchingHeader(currentHeaders, headerName);
            if (optionalMatchingHeaderName.isPresent()) {
                String matchingHeaderName = optionalMatchingHeaderName.get();
                currentHeaders.remove(matchingHeaderName);
                currentHeaders.add(headerName.toLowerCase(), headerValue);
            } else {
                currentHeaders.add(headerName.toLowerCase(), headerValue);
            }
        });
    }

    // Returns the matching header name
    private static Optional<String> matchingHeader(HttpHeaders headers, String targetHeaderName) {
        for (String headerName : headers.names()) {
            if (headerName.toLowerCase().equals(targetHeaderName.toLowerCase())) {
                return Optional.of(headerName);
            }
        }
        return Optional.empty();
    }
}
