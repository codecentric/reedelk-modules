package com.esb.commons;

public enum FileExtension {

    XML("xml"),
    JSON("json"),
    FLOW("flow"),
    SUBFLOW("subflow"),
    FLOW_CONFIG("fconfig"),
    PROPERTIES("properties");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
