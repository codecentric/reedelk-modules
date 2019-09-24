package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;

abstract class AbstractBodyProvider<T> {

    private final DynamicValue body;
    private final ScriptEngineService scriptEngine;

    AbstractBodyProvider(ScriptEngineService scriptEngine, DynamicValue body) {
        this.body = body;
        this.scriptEngine = scriptEngine;
    }

    public T from(Message message, FlowContext flowContext) {
        // This code is only executed if and only if the request is
        // either POST,PUT or DELETE. For all other HTTP methods this is not executed.

        if (body == null || body.isBlank()) {
            // If the script is empty, there is nothing to evaluate.
            // No content type header
            return empty();
        } else if (body.isMessagePayload()) {
            // Content == Payload
            return fromContent(message.getContent());
        } else {
            // The is a script: we evaluate it and set it the result.
            // No content type header, it is set by the user
            Object result = scriptEngine.evaluate(body, message, flowContext);
            byte[] bodyAsBytes = result.toString().getBytes();
            return fromBytes(bodyAsBytes);
        }
    }

    protected abstract T empty();

    protected abstract T fromBytes(byte[] bytes);

    protected abstract T fromContent(TypedContent<?> content);
}
