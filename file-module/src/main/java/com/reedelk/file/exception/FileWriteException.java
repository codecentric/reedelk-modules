package com.reedelk.file.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class FileWriteException extends ESBException {
    public FileWriteException(String message, Throwable exception) {
        super(message, exception);
    }
}
