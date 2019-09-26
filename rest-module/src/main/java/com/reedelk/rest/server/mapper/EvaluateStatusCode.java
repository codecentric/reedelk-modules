package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicInteger;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;

class EvaluateStatusCode {

    private final HttpResponseStatus defaultCode;

    private Message message;
    private Throwable throwable;
    private DynamicInteger status;
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

    EvaluateStatusCode withStatus(DynamicInteger status) {
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

    /**
     * If the message is defined, then we use the message, otherwise the exception.
     * @return the evaluated response status code.
     */
    HttpResponseStatus evaluate() {
        // If Message is defined, then use message, otherwise the exception.
        if (message != null) {
            // TODO: Test what happens if status is not present.
            return scriptEngine.evaluate(status, message, flowContext)
                    .flatMap(status -> Optional.of(valueOf(status)))
                    .orElse(defaultCode);

        } else if (throwable != null) {
            // TODO: Test what happens if status is not present.
            return scriptEngine.evaluate(status, throwable, flowContext)
                    .flatMap(status -> Optional.of(valueOf(status)))
                    .orElse(defaultCode);

        } else {
            throw new ESBException("error: Message or Throwable must be defined");
        }
    }
}
