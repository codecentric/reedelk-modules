package com.reedelk.rest;

import com.reedelk.runtime.api.exception.ESBException;

public class ExecutionException extends ESBException {

    public ExecutionException(String errorMessage) {
        super(errorMessage);
    }

    public ExecutionException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
}
