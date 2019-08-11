package com.reedelk.esb.test.utils;

import com.reedelk.runtime.api.component.Inbound;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;

public class TestInboundComponent implements Inbound {

    private String message;

    @Override
    public void onStart() {
        throw new UnsupportedOperationException("Test Only Inbound");
    }

    @Override
    public void onShutdown() {
        throw new UnsupportedOperationException("Test Only Inbound");
    }

    @Override
    public void onEvent(Message message, OnResult callback) {
        throw new UnsupportedOperationException("Test Only Inbound");
    }

    @Override
    public void removeEventListener() {
        throw new UnsupportedOperationException("Test Only Inbound");
    }

    @Override
    public void addEventListener(InboundEventListener listener) {
        throw new UnsupportedOperationException("Test Only Inbound");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
