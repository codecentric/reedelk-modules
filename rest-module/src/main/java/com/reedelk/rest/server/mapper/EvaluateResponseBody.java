package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

class EvaluateResponseBody {

    private final DynamicByteArray responseBody;

    private Message message;
    private FlowContext flowContext;
    private ScriptEngineService scriptEngine;

    private EvaluateResponseBody(DynamicByteArray responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateResponseBody withResponseBody(DynamicByteArray responseBody) {
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
        return scriptEngine.evaluateStream(responseBody, message, flowContext);
    }
}
