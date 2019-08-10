package com.esb.rest.commons;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.Map;
import java.util.TreeMap;

public class HttpHeadersAsMap {

    public static Map<String, String> of(HttpHeaders headers) {
        Map<String,String> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headers.get(headerName)));
        return requestHeaders;
    }
}
