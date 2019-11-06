package com.reedelk.file.commons;

public class ReadOptions {

    private final LockType lockType;
    private final int maxRetryAttempts;
    private final long maxRetryWaitTime;

    public ReadOptions(LockType lockType, int maxRetryAttempts, long maxRetryWaitTime) {
        this.lockType = lockType;
        this.maxRetryAttempts = maxRetryAttempts;
        this.maxRetryWaitTime = maxRetryWaitTime;
    }

    public LockType getLockType() {
        return lockType;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public long getMaxRetryWaitTime() {
        return maxRetryWaitTime;
    }
}
