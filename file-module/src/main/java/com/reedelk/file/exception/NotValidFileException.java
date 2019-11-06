package com.reedelk.file.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class NotValidFileException extends ESBException {
    public NotValidFileException(String message) {
        super(message);
    }
}
