package com.reedelk.rest.server.mapper;

public interface HttpRequestAttribute {

    static String remoteAddress() {
        return "remoteAddress";
    }

    static String matchingPath() {
        return "matchingPath";
    }

    static String queryParams() {
        return "queryParams";
    }

    static String requestPath() {
        return "requestPath";
    }

    static String requestUri() {
        return "requestUri";
    }

    static String queryString() {
        return "queryString";
    }

    static String pathParams() {
        return "pathParams";
    }

    static String version() {
        return "version";
    }

    static String headers() {
        return "headers";
    }

    static String scheme() {
        return "scheme";
    }

    static String method() {
        return "method";
    }
}
