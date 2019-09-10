package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class HttpHeadersAsMap {

    public static TreeMap<String, List<String>> of(HttpHeaders headers) {
        TreeMap<String, List<String>> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headerValue(headers, headerName)));
        return requestHeaders;
    }

    private static List<String> headerValue(HttpHeaders headers, String headerName) {
        String headerValue = headers.get(headerName);
        if (StringUtils.isBlank(headerValue)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(headerValue.split(","))
                    .map(StringUtils::trim)
                    .collect(Collectors.toList());
        }
    }
}
