package com.reedelk.file.commons;

import com.reedelk.runtime.api.exception.ESBException;

import java.util.function.Supplier;

class RetryCommand<T> {

    private final long waitTime;
    private final int maxRetries;

    RetryCommand(int maxRetries, long waitTime) {
        this.maxRetries = maxRetries;
        this.waitTime = waitTime;

    }

    void run(Supplier<T> function, Class<? extends Exception> retryOnException) {
        try {
            function.get();
        } catch (Exception e) {
            if (retryOnException.isAssignableFrom(e.getClass())) {
                retry(function, retryOnException);
            }
            throw e;
        }
    }

    private T retry(Supplier<T> function, Class<? extends Exception> retryOnException) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {

                Thread.sleep(waitTime);

                return function.get();

            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
                break;

            } catch (Exception ex) {
                if (retryOnException.isAssignableFrom(ex.getClass())) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        System.out.println("Max retries exceeded.");
                        break;
                    }
                } else {
                    // Exception thrown for which we cant' retry.
                    System.out.println(ex.getMessage());
                    break;
                }
            }
        }
        throw new ESBException("Command failed " + maxRetries + " retries");
    }
}
