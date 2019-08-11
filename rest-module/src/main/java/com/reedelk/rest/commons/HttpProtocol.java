package com.reedelk.rest.commons;

public enum HttpProtocol {

    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    private final String value;

    HttpProtocol(String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }
}
