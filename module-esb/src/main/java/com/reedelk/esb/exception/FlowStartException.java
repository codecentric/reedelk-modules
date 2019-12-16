package com.reedelk.esb.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class FlowStartException extends ESBException {
    public FlowStartException(String message, Exception exception) {
        super(message, exception);
    }
}
