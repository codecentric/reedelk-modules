package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicInteger;
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
     * Evaluates the dynamic value defined for the status code.
     * If the message is defined, then the message is used, otherwise
     * the exception is used instead.
     * @return the evaluated response status code.
     */
    HttpResponseStatus evaluate() {
        if (message != null) {
            return scriptEngine.evaluate(status, flowContext, message)
                    .flatMap(status -> Optional.of(valueOf(status)))
                    .orElse(defaultCode);

        } else if (throwable != null) {
            return scriptEngine.evaluate(status, flowContext, throwable)
                    .flatMap(status -> Optional.of(valueOf(status)))
                    .orElse(defaultCode);

        } else {
            throw new ESBException("error: Message or Throwable must be defined");
        }
    }
}
