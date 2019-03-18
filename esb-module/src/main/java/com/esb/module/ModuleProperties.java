package com.esb.module;

public class ModuleProperties {

    private ModuleProperties() {
    }

    public static class Bundle {
        public static final String MODULE_HEADER_NAME = "ESB-Module";
    }

    public static class Flow {
        public static final String RESOURCE_DIRECTORY = "/flows";
        public static final String ROOT_PROPERTY = "flow";
    }

    public static class Subflow {
        public static final String RESOURCE_DIRECTORY = "/flows";
        public static final String ROOT_PROPERTY = "subflow";
    }

    public static class Config {
        public static final String RESOURCE_DIRECTORY = "/configs";
        public static final String ROOT_PROPERTY = "configs";
    }

}
