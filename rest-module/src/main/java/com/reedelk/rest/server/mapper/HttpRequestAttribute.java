package com.reedelk.rest.server.mapper;

public interface HttpRequestAttribute {

    static String path() {
        return "path";
    }

    static String method() {
        return "method";
    }

    static String headers() {
        return "headers";
    }

    static String pathParams() {
        return "pathParams";
    }

    static String queryParams() {
        return "queryParams";
    }
}
