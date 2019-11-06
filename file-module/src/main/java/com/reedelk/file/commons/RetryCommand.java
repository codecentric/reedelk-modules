package com.reedelk.file.commons;

import com.reedelk.runtime.api.exception.ESBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

class RetryCommand {

    private static final Logger logger = LoggerFactory.getLogger(RetryCommand.class);

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

            } catch (InterruptedException ex) {

                String errorMessage = "Could not read file: " + ex.getMessage();
                logger.warn(errorMessage, ex);
                throw new ESBException(errorMessage);

            } catch (Exception ex) {

                if (retryOnException.isAssignableFrom(ex.getClass())) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        String errorMessage = "Could not read file: max retries exceeded";
                        logger.warn(errorMessage, ex);
                        throw new ESBException(errorMessage);
                    }

                } else {
                    // Exception thrown for which we cant' retry.
                    String errorMessage = "Could not read file: " + ex.getMessage();
                    logger.warn(errorMessage, ex);
                    throw new ESBException(errorMessage);
                }
            }
        }
    }

    static class Builder {

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
