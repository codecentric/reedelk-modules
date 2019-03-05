package com.esb.commons;

public class ModuleProperties {

    private ModuleProperties() {
    }

    public static class Bundle {
        public static final String MODULE_HEADER_NAME = "ESB-Module";
    }

    public static class Flow {
        public static final String RESOURCE_DIRECTORY = "/flows";
    }

    public static class Config {
        public static final String RESOURCE_DIRECTORY = "/configs";
    }

}
