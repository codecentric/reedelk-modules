package com.esb.test.utils;

import com.esb.api.component.Inbound;
import com.esb.api.component.InboundEventListener;
import com.esb.api.component.OnResult;
import com.esb.api.message.Message;

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
