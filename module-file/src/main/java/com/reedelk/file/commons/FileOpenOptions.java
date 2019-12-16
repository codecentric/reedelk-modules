package com.reedelk.file.commons;

import java.nio.file.OpenOption;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public class FileOpenOptions {

    public static OpenOption[] from(FileOperation fileOperation, LockType lockType) {
        if (LockType.LOCK.equals(lockType)) {
            return new OpenOption[] { READ, WRITE };
        } else if (FileOperation.READ.equals(fileOperation)) {
            return new OpenOption[] { READ };
        } else {
            return new OpenOption[] { WRITE };
        }
    }
}
