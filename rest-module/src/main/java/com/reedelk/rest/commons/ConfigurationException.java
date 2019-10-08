package com.reedelk.rest.commons;

import com.reedelk.runtime.api.exception.ESBException;

public class ConfigurationException extends ESBException {
    public ConfigurationException(String errorMessage) {
        super(errorMessage);
    }
}