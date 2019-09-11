package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.Evaluate;
import com.reedelk.rest.commons.StackTraceUtils;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.SimpleBindings;

class EvaluateErrorResponseBody {

    private final String responseBody;

    private Throwable exception;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateErrorResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateErrorResponseBody withResponseBody(String responseBody) {
        return new EvaluateErrorResponseBody(responseBody);
    }

    EvaluateErrorResponseBody withThrowable(Throwable exception) {
        this.exception = exception;
        return this;
    }

    EvaluateErrorResponseBody withContext(FlowContext flowContext) {
        this.flowContext = flowContext;
        return this;
    }

    EvaluateErrorResponseBody withScriptEngine(ScriptEngineService scriptEngine) {
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
        if  (Evaluate.isErrorPayload(responseBody)) {
            // We avoid evaluating a script if we just want
            // to return the exception stacktrace (optimization).
            return StackTraceUtils.asByteStream(exception);
        } else if (ScriptUtils.isEmpty(responseBody)) {
            return Mono.empty();
        } else {
            return evaluateBodyScript();
        }
    }

    private Publisher<byte[]> evaluateBodyScript() {
        try {
            SimpleBindings additionalBindings = new SimpleBindings();
            additionalBindings.put("error", exception);
            ScriptExecutionResult result =
                    scriptEngine.evaluate(responseBody, flowContext, additionalBindings);

            // TODO: Test the script what it might return something different from string?? and stuff...
            Object object = result.getObject();
            return Mono.just(object.toString().getBytes());
        } catch (Exception exception) {
            // Evaluating an error response, cannot throw again an exception,
            // Hence we catch any exception and we return the exception message.
            return StackTraceUtils.asByteStream(exception);
        }
    }
}
