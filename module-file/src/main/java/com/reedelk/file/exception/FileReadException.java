package com.reedelk.file.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class FileReadException extends ESBException {
    public FileReadException(String message, Throwable exception) {
        super(message, exception);
    }
}
