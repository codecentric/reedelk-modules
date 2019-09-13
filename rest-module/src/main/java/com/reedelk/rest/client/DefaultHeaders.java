package com.reedelk.rest.client;

import reactor.netty.http.client.HttpClientRequest;

import java.util.HashMap;
import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.USER_AGENT;

public class DefaultHeaders {

    private static final Map<String,String> DEFAULT_HEADERS;
    static {
        Map<String,String> tmp = new HashMap<>();
        tmp.put(USER_AGENT,UserAgent.NAME);
        DEFAULT_HEADERS = tmp;
    }

    public static void add(HttpClientRequest request) {
        DEFAULT_HEADERS.forEach(request::addHeader);
    }
}
