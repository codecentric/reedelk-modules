package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;

abstract class AbstractBodyProvider<T> {

    private final String body;
    private final ScriptEngineService scriptEngine;

    AbstractBodyProvider(ScriptEngineService scriptEngine, String body) {
        this.body = body;
        this.scriptEngine = scriptEngine;
    }

    public T from(Message message, FlowContext flowContext) {
        // This code is only executed if and only if the request is
        // either POST,PUT or DELETE. For all other HTTP methods this is not executed.
        if (StringUtils.isBlank(body)) {
            // No content type header
            return empty();
        } else if (ScriptUtils.isScript(body)) {
            return fromScript(message, body, flowContext, scriptEngine);
        } else {
            return fromText(body);
        }
    }

    private T fromScript(Message message, String body, FlowContext flowContext, ScriptEngineService scriptEngine) {
        if (ScriptUtils.isMessagePayload(body)) {
            // Content == Payload
            return fromContent(message.getContent());
        } else if (ScriptUtils.isEmpty(body)) {
            // If the script is empty, there is nothing to evaluate.
            // No content type header
            return empty();
        } else {
            // The is a script: we evaluate it and set it the result.
            // No content type header, it is set by the user
            Object result = scriptEngine.evaluate(body, message, flowContext);
            byte[] bodyAsBytes = result.toString().getBytes();
            return fromBytes(bodyAsBytes);
        }
    }

    private T fromText(String body) {
        // The body is not a script, it is just plain text.
        // We know the number of bytes to be sent.
        // Transfer encoding will NOT be chunked.
        byte[] bodyAsBytes = body.getBytes();
        return fromBytes(bodyAsBytes);
    }

    protected abstract T empty();

    protected abstract T fromBytes(byte[] bytes);

    protected abstract T fromContent(TypedContent<?> content);
}
