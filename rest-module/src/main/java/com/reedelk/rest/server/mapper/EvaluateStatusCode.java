package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;

class EvaluateStatusCode {

    private final HttpResponseStatus defaultCode;

    private Message message;
    private Throwable throwable;
    private DynamicValue status;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateStatusCode(HttpResponseStatus defaultCode) {
        this.defaultCode = defaultCode;
    }

    static EvaluateStatusCode withDefault(HttpResponseStatus defaultCode) {
        return new EvaluateStatusCode(defaultCode);
    }

    EvaluateStatusCode withMessage(Message message) {
        this.message = message;
        return this;
    }

    EvaluateStatusCode withStatus(DynamicValue status) {
        this.status = status;
        return this;
    }

    EvaluateStatusCode withThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    EvaluateStatusCode withContext(FlowContext flowContext) {
        this.flowContext = flowContext;
        return this;
    }

    EvaluateStatusCode withScriptEngine(ScriptEngineService scriptEngine) {
        this.scriptEngine = scriptEngine;
        return this;
    }

    HttpResponseStatus evaluate() {
        if (status == null || status.isBlank()) {
            return defaultCode;
        }

        // If Message is defined, then use message,
        // Otherwise we use the exception
        // TODO: Evaluate if it is text and Integer.valueOf throws an exception is should return error.
        if (message != null) {
            if (status.isScript()) {
                int evaluate = scriptEngine.evaluate(status, message, flowContext);
                return valueOf(evaluate);
            } else {
                return valueOf(Integer.valueOf(status.getBody()));
            }
        }

        if (throwable != null) {
            if (status.isScript()) {
                int evaluate = scriptEngine.evaluate(status, throwable, flowContext);
                return valueOf(evaluate);
            } else {
                return valueOf(Integer.valueOf(status.getBody()));
            }
        }

        throw new ESBException("error: Message or Throwable must be defined");
    }
}
