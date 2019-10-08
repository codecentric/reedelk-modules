package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.ESBException;

public class Preconditions {

    private Preconditions() {
    }

    static <T> T requireNotNull(T object, String errorMessage) {
        if (object == null) {
            throw new ESBException(errorMessage);
        }
        return object;
    }

    static String requireNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ESBException(message);
        }
        return value;
    }
}
