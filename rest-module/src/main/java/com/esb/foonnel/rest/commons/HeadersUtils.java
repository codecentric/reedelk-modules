package com.esb.foonnel.rest.commons;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class HeadersUtils {

    public static Map<String,String> toMap(HttpHeaders headers) {
        Map<String,String> requestHeaders = new HashMap<>();
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headers.get(headerName)));
        return requestHeaders;
    }
}
