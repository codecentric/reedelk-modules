package com.reedelk.rest;

import com.reedelk.runtime.api.exception.ESBException;

public class ServerTooBusyException extends ESBException {

    public ServerTooBusyException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
