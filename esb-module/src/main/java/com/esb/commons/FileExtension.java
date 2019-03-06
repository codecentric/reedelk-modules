package com.esb.commons;

public enum FileExtension {

    XML("xml"),
    JSON("json"),
    PROPERTIES("properties");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
