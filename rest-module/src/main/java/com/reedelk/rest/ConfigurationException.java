package com.reedelk.rest;

import com.reedelk.runtime.api.exception.ESBException;

public class ConfigurationException extends ESBException {

    public ConfigurationException(String errorMessage) {
        super(errorMessage);
    }

    public ConfigurationException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
}
