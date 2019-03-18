package com.esb.commons;

public enum FileExtension {

    XML("xml"),
    JSON("json"),
    FLOW("flow"),
    FLOW_CONFIG("flow.config"),
    PROPERTIES("properties");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
