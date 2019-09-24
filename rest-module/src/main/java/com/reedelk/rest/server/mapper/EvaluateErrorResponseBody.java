package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.Evaluate;
import com.reedelk.rest.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

class EvaluateErrorResponseBody {

    private final DynamicValue responseBody;

    private Throwable exception;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateErrorResponseBody(DynamicValue responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateErrorResponseBody withResponseBody(DynamicValue responseBody) {
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
        if (responseBody == null|| responseBody.isBlank()) {
            return Mono.empty();
        } else if  (Evaluate.isErrorPayload(responseBody)) {
            // We avoid evaluating a script if we just want
            // to return the exception stacktrace (optimization).
            return StackTraceUtils.asByteStream(exception);
        } else {
            return evaluateBodyScript();
        }
    }

    private Publisher<byte[]> evaluateBodyScript() {
        try {
            Object result =
                    scriptEngine.evaluate(responseBody, exception, flowContext);
            return Mono.just(result.toString().getBytes());
        } catch (Exception exception) {
            // Evaluating an error response, cannot throw again an exception,
            // Hence we catch any exception and we return the exception message.
            return StackTraceUtils.asByteStream(exception);
        }
    }
}
