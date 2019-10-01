package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

class EvaluateErrorResponseBody {

    private final DynamicByteArray responseBody;

    private Throwable exception;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateErrorResponseBody(DynamicByteArray responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateErrorResponseBody withResponseBody(DynamicByteArray responseBody) {
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
        try {
            return scriptEngine.evaluateStream(responseBody, exception, flowContext);
        } catch (Exception exception) {
            // Evaluating an error response, cannot throw again an exception,
            // Hence we catch any exception and we return the exception message.
            return StackTraceUtils.asByteStream(exception);
        }
    }
}
