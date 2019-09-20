package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class BodyStreamProvider {

    public static Publisher<byte[]> from(Message message, FlowContext flowContext, String body, ScriptEngineService scriptEngine) {
        // This code is only executed if and only if the request is
        // either POST,PUT or DELETE. For all other HTTP methods this is not executed.
        if (StringUtils.isBlank(body)) {
            // No content type header
            return Mono.empty();
        } else if (ScriptUtils.isScript(body)) {
            return fromScript(message, body, flowContext, scriptEngine);
        } else {
            return fromText(body);
        }
    }

    private static Publisher<byte[]> fromScript(Message message, String body, FlowContext flowContext, ScriptEngineService scriptEngine) {
        if (ScriptUtils.isMessagePayload(body)) {

            if (message.getContent().isStream()) {
                // The payload is a stream based payload.
                // We don't know the content length, since it is not loaded into memory.
                // In this case the transfer encoding will be chunked (the length is not set).
                return message.getContent().asByteArrayStream();
            } else {
                // The payload is not a stream based payload.
                // We know the content length, since it is completely loaded into memory.
                byte[] bodyAsBytes = message.getContent().asByteArray();
                return Mono.just(bodyAsBytes);
            }
        } else if (ScriptUtils.isEmpty(body)) {
            // If the script is empty, there is nothing to evaluate.
            // No content type header
            return Mono.empty();

        } else {
            // The is a script: we evaluate it and set it the result.
            // No content type header, it is set by the user
            Object result = scriptEngine.evaluate(body, message, flowContext);
            byte[] bodyAsBytes = result.toString().getBytes();
            return Mono.just(bodyAsBytes);
        }
    }

    private static Publisher<byte[]> fromText(String body) {
        // The body is not a script, it is just plain text.
        // We know the number of bytes to be sent.
        // Transfer encoding will NOT be chunked.
        byte[] bodyAsBytes = body.getBytes();
        return Mono.just(bodyAsBytes);
    }
}
