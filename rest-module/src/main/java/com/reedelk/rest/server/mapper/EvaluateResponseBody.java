package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.ScriptException;

class EvaluateResponseBody {

    private final String responseBody;
    private ScriptEngineService scriptEngine;
    private Message message;
    private FlowContext flowContext;

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

        // Response body
        if (ScriptUtils.isScript(responseBody)) {
            return bodyStreamFromScript();
        } else {
            // It is just plain text
            return Mono.just(responseBody.getBytes());
        }

    }

    private Publisher<byte[]> bodyStreamFromScript() {
        if (isBodyMessagePayload(responseBody)) {
            return message.getContent().asByteArrayStream();
        }else if (ScriptUtils.isEmpty(responseBody)) {
         return Mono.empty();
            } else {
            try {
                ScriptExecutionResult result =
                        scriptEngine.evaluate(responseBody, message, flowContext);
                Object object = result.getObject();
                return Mono.just(object.toString().getBytes());
            } catch (ScriptException e) {
                return Mono.just(e.getMessage().getBytes());
            }
        }
    }

    private boolean isBodyMessagePayload(String responseBody) {
        String unwrappedScript = ScriptUtils.unwrap(responseBody);
        return "payload".equals(StringUtils.trim(unwrappedScript));
    }
}
