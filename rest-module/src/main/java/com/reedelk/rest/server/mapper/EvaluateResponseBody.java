package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

class EvaluateResponseBody {

    private final DynamicValue responseBody;

    private Message message;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateResponseBody(DynamicValue responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateResponseBody withResponseBody(DynamicValue responseBody) {
        return new EvaluateResponseBody(responseBody);
    }

    EvaluateResponseBody withMessage(Message message) {
        this.message = message;
        return this;
    }

    EvaluateResponseBody withContext(FlowContext flowContext) {
        this.flowContext = flowContext;
        return this;
    }

    EvaluateResponseBody withScriptEngine(ScriptEngineService scriptEngine) {
        this.scriptEngine = scriptEngine;
        return this;
    }

    Publisher<byte[]> evaluate() {
        return responseBody == null ?
                Mono.empty():
                bodyStreamFromScript();
    }

    private Publisher<byte[]> bodyStreamFromScript() {
        if (responseBody == null || responseBody.isBlank()) {
            return Mono.empty();
        } else if (responseBody.isMessagePayload()) {
            // We avoid evaluating a script if we just want
            // to return the message payload (optimization).
            return message.getContent().asByteArrayStream();
        } else {
            Object result = scriptEngine.evaluate(responseBody, message, flowContext);
            return Mono.just(result.toString().getBytes());
        }
    }
}
