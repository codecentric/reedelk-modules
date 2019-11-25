package com.reedelk.esb.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class FlowExecutionException extends ESBException {

    public FlowExecutionException(String message, Throwable exception) {
        super(message, exception);
    }
}
