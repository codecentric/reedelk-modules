package com.reedelk.rest.server;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.ScriptException;

public class EvaluateResponseBody {

    private final String responseBody;
    private ScriptEngineService scriptEngine;
    private Message message;
    private FlowContext flowContext;

    private EvaluateResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public static EvaluateResponseBody withResponseBody(String responseBody) {
        return new EvaluateResponseBody(responseBody);
    }

    public EvaluateResponseBody withMessage(Message message) {
        this.message = message;
        return this;
    }

    public EvaluateResponseBody withContext(FlowContext flowContext) {
        this.flowContext = flowContext;
        return this;
    }

    public EvaluateResponseBody withScriptEngine(ScriptEngineService scriptEngine) {
        this.scriptEngine = scriptEngine;
        return this;
    }

    public Publisher<byte[]> evaluate() {
        // Response body
        if (responseBody != null) {
            // Custom body - evaluate script - or just return the value (if it is not a script)
            try {
                ScriptExecutionResult result =
                        scriptEngine.evaluate(responseBody, message, flowContext);
                Object object = result.getObject();
                return Mono.just(object.toString().getBytes());
            } catch (ScriptException e) {
                return Mono.just(e.getMessage().getBytes());
            }

        } else {
            // The content type comes from the message typed content
            TypedContent<?> typedContent = message.getContent();
            return typedContent.asByteArrayStream();
        }
    }
}
