package com.esb.services.scriptengine;

import com.esb.api.message.Message;

import javax.script.SimpleBindings;

/**
 * Default Context Variables available during the
 * execution of a Javascript script.
 */
class ContextVariables extends SimpleBindings {
    ContextVariables(Message message) {
        put("message", message);
        put("inboundProperties", message.getInboundProperties());
        put("outboundProperties", message.getOutboundProperties());

        if (message.getTypedContent() != null) {
            put("payload", message.getTypedContent().getContent());
        } else {
            put("payload", null);
        }
    }
}