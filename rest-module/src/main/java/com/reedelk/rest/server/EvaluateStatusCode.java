package com.reedelk.rest.server;

import com.reedelk.rest.commons.StringUtils;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;

public class EvaluateStatusCode {

    private final HttpResponseStatus defaultCode;

    private Message message;
    private Throwable throwable;
    private String statusAsString;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    public EvaluateStatusCode(HttpResponseStatus defaultCode) {
        this.defaultCode = defaultCode;
    }

    public static EvaluateStatusCode withDefault(HttpResponseStatus defaultCode) {
        return new EvaluateStatusCode(defaultCode);
    }

    public EvaluateStatusCode withMessage(Message message) {
        this.message = message;
        return this;
    }

    public EvaluateStatusCode withStatus(String statusAsString) {
        this.statusAsString = statusAsString;
        return this;
    }

    public EvaluateStatusCode withThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public EvaluateStatusCode withContext(FlowContext flowContext) {
        this.flowContext = flowContext;
        return this;
    }

    public EvaluateStatusCode withScriptEngine(ScriptEngineService scriptEngine) {
        this.scriptEngine = scriptEngine;
        return this;
    }

    public HttpResponseStatus evaluate() {
        if (StringUtils.isBlank(statusAsString)) {
            return defaultCode;
        }

        if (ScriptUtils.isScript(statusAsString)) {
            // If Message is defined, then use message,
            // Otherwise we use the exception
            if (message != null) {
                try {
                    int evaluate = scriptEngine.evaluate(message, statusAsString);
                    return valueOf(evaluate);
                } catch (ScriptException e) {
                    e.printStackTrace();
                    throw new ESBException("Error", e);
                }
            }

            if (throwable != null) {
                SimpleBindings bindings = new SimpleBindings();
                bindings.put("error", throwable);
                try {
                    int evaluate = scriptEngine.evaluate(statusAsString, bindings);
                    return valueOf(evaluate);
                } catch (ScriptException e) {
                    e.printStackTrace();
                    throw new ESBException("Error", e);
                }
            }

            throw new ESBException("Error, Message or throwable must be defined");

        } else {
            int code = Integer.valueOf(statusAsString);
            return valueOf(code);
        }
    }
}
