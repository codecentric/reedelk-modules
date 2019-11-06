package com.reedelk.file.commons;

import com.reedelk.file.commons.Messages.Misc;
import com.reedelk.file.exception.MaxRetriesExceeded;
import com.reedelk.runtime.api.exception.ESBException;

import java.util.function.Supplier;

public class RetryCommand {

    private final long waitTime;
    private final int maxRetries;
    private final Supplier<?> function;
    private final Class<? extends Exception> retryOnException;

    public static Builder builder() {
        return new Builder();
    }

    private RetryCommand(Supplier<?> function, int maxRetries, long waitTime, Class<? extends Exception> retryOnException) {
        this.waitTime = waitTime;
        this.function = function;
        this.maxRetries = maxRetries;
        this.retryOnException = retryOnException;

    }

    public void execute() {
        try {
            function.get();
        } catch (Exception exception) {

            if (retryOnException.isAssignableFrom(exception.getClass())) {
                // We only retry if the exception thrown is expected
                // and eligible for the retry.
                retry();

            } else {
                throw exception;
            }
        }
    }

    private void retry() {

        int attempt = 0;

        while (attempt < maxRetries) {

            try {

                Thread.sleep(waitTime);

                function.get();

            } catch (InterruptedException exception) {

                throw new ESBException(exception);

            } catch (Exception exception) {

                if (retryOnException.isAssignableFrom(exception.getClass())) {

                    attempt++;

                    if (attempt >= maxRetries) {

                        throw new MaxRetriesExceeded(Misc.MAX_ATTEMPTS_EXCEEDED.format(maxRetries));

                    }

                    // Otherwise we keep attempting the retry ...

                } else {

                    throw new ESBException(exception);

                }
            }
        }
    }

    public static class Builder {

        private long waitTime;
        private int maxRetries;
        private Supplier<?> function;
        private Class<? extends Exception> retryOnException;

        public Builder function(Supplier<?> function) {
            this.function = function;
            return this;
        }

        public Builder retryOn(Class<? extends Exception> retryOnException) {
            this.retryOnException = retryOnException;
            return this;
        }

        public Builder waitTime(long waitTime) {
            this.waitTime = waitTime;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryCommand build() {
            return new RetryCommand(function, maxRetries, waitTime, retryOnException);
        }
    }
}
