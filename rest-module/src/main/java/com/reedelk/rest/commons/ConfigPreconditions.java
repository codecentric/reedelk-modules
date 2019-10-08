package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;

public class ConfigPreconditions {

    private ConfigPreconditions() {
    }

    public static <T> T requireNotNull(T object, String errorMessage) {
        if (object == null) {
            throw new ConfigurationException(errorMessage);
        }
        return object;
    }

    public static String requireNotBlank(String value, String errorMessage) {
        if (StringUtils.isBlank(value)) {
            throw new ConfigurationException(errorMessage);
        }
        return value;
    }
}
