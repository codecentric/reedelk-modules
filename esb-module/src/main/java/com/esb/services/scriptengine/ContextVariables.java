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
        put("payload", message.getTypedContent().getContent());
        put("inboundProperties", message.getInboundProperties());
        put("outboundProperties", message.getOutboundProperties());
    }
}