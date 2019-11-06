package com.reedelk.file.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class MaxRetriesExceeded extends ESBException {
    public MaxRetriesExceeded(String message) {
        super(message);
    }
}
