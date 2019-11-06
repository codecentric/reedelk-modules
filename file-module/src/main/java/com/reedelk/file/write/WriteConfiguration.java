package com.reedelk.file.write;

import com.reedelk.file.commons.LockType;

import java.util.Optional;

import static com.reedelk.file.commons.Defaults.FileWrite.*;

public class WriteConfiguration {

    private final LockType lockType;
    private final WriteMode writeMode;

    private final int writeBufferSize;
    private final int retryMaxAttempts;
    private final long retryWaitTime;
    private final boolean createParentDirectory;

    public WriteConfiguration(FileWriteConfiguration configuration) {
        this.lockType = getLockType(configuration);
        this.writeMode = getWriteMode(configuration);

        this.writeBufferSize = getWriteBufferSize(configuration);
        this.retryMaxAttempts = getRetryMaxAttempts(configuration);
        this.retryWaitTime = getRetryWaitTime(configuration);
        this.createParentDirectory = getCreateParentDirectory(configuration);
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

    int getWriteBufferSize() {
        return writeBufferSize;
    }

    WriteMode getWriteMode() {
        return writeMode;
    }

    public boolean isCreateParentDirectory() {
        return createParentDirectory;
    }

    private int getWriteBufferSize(FileWriteConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getWriteBufferSize()))
                .orElse(WRITE_FILE_BUFFER_SIZE);
    }

    private LockType getLockType(FileWriteConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getLockFile()))
                .map(shouldLock -> shouldLock ? LockType.LOCK : LockType.NONE)
                .orElse(LockType.NONE);
    }

    private int getRetryMaxAttempts(FileWriteConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getLockRetryMaxAttempts()))
                .orElse(RETRY_MAX_ATTEMPTS);
    }

    private long getRetryWaitTime(FileWriteConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getLockRetryWaitTime()))
                .orElse(RETRY_WAIT_TIME);
    }

    private boolean getCreateParentDirectory(FileWriteConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getCreateParentDirectory()))
                .orElse(false);
    }

    private WriteMode getWriteMode(FileWriteConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getMode()))
                .orElse(WriteMode.OVERWRITE);
    }
}
