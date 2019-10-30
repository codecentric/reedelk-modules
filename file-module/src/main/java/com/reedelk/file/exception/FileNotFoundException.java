package com.reedelk.file.exception;

import com.reedelk.runtime.api.exception.ESBException;

public class FileNotFoundException extends ESBException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
