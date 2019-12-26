package com.reedelk.file.read;

import com.reedelk.file.commons.LockType;

import java.util.Optional;

import static com.reedelk.file.commons.Defaults.FileRead.*;

public class ReadConfigurationDecorator {

    private final LockType lockType;
    private final int readBufferSize;
    private final long retryWaitTime;
    private final int retryMaxAttempts;

    public ReadConfigurationDecorator(FileReadConfiguration configuration) {
        this.lockType = getLockType(configuration);
        this.readBufferSize = getReadBufferSize(configuration);
        this.retryMaxAttempts = getRetryMaxAttempts(configuration);
        this.retryWaitTime = getRetryWaitTime(configuration);
    }

    LockType getLockType() {
        return lockType;
    }

    int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    long getRetryWaitTime() {
        return retryWaitTime;
    }

    int getReadBufferSize() {
        return readBufferSize;
    }

    private int getReadBufferSize(FileReadConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getReadBufferSize()))
                .orElse(READ_FILE_BUFFER_SIZE);
    }

    private LockType getLockType(FileReadConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getLockFile()))
                .map(shouldLock -> shouldLock ? LockType.LOCK : LockType.NONE)
                .orElse(LockType.NONE);
    }

    private int getRetryMaxAttempts(FileReadConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getLockRetryMaxAttempts()))
                .orElse(RETRY_MAX_ATTEMPTS);
    }

    private long getRetryWaitTime(FileReadConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getLockRetryWaitTime()))
                .orElse(RETRY_WAIT_TIME);
    }
}
