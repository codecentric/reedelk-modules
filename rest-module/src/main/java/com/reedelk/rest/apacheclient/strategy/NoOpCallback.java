package com.reedelk.rest.apacheclient.strategy;

import org.apache.http.concurrent.FutureCallback;

public class NoOpCallback implements FutureCallback<Void> {

    public static final NoOpCallback INSTANCE =  new NoOpCallback();

    private NoOpCallback() {
    }

    @Override
    public void completed(Void result) {
        // no op
    }

    @Override
    public void failed(Exception ex) {
        // no op
    }

    @Override
    public void cancelled() {
        // no op
    }
}
