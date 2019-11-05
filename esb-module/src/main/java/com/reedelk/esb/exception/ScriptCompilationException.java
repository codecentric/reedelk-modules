package com.reedelk.esb.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class ScriptCompilationException extends ESBException {
    public ScriptCompilationException(String message, Exception exception) {
        super(message, exception);
    }
}
