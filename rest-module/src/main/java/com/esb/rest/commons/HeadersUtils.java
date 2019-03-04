package com.esb.rest.commons;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.Map;
import java.util.TreeMap;

public class HeadersUtils {

    public static Map<String,String> toMap(HttpHeaders headers) {
        Map<String,String> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headers.get(headerName)));
        return requestHeaders;
    }
}
