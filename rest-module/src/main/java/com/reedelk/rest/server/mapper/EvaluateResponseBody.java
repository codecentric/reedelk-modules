package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

class EvaluateResponseBody {

    private final String responseBody;

    private Message message;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateResponseBody withResponseBody(String responseBody) {
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
        if (responseBody == null) return Mono.empty();

        if (ScriptUtils.isScript(responseBody)) {
            // The body is a script
            return bodyStreamFromScript();
        } else {
            // It is just plain text
            return Mono.just(responseBody.getBytes());
        }

    }

    private Publisher<byte[]> bodyStreamFromScript() {
        if (ScriptUtils.isMessagePayload(responseBody)) {
            // We avoid evaluating a script if we just want
            // to return the message payload (optimization).
            return message.getContent().asByteArrayStream();
        } else if (ScriptUtils.isEmpty(responseBody)) {
            return Mono.empty();
        } else {
            return evaluateBodyScript();
        }
    }

    private Publisher<byte[]> evaluateBodyScript() {
        ScriptExecutionResult result =
                scriptEngine.evaluate(responseBody, message, flowContext);

        // TODO: Test the script what it might return something different from string?? and stuff...
        Object object = result.getObject();
        return Mono.just(object.toString().getBytes());
    }
}
