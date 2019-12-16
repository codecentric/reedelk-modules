package com.reedelk.esb.commons;

import com.reedelk.runtime.api.message.Message;

import java.util.UUID;

import static com.reedelk.runtime.api.message.MessageAttributeKey.CORRELATION_ID;

public class CorrelationIDFromMessageOrNew {

    private CorrelationIDFromMessageOrNew() {
    }

    public static String from(Message message) {
        return message.getAttributes().contains(CORRELATION_ID) ?
                message.getAttributes().get(CORRELATION_ID) :
                UUID.randomUUID().toString();
    }
}
