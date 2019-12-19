package com.reedelk.rest.server.body;

import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerResponse;

public class BodyProviderStreamAlways implements BodyProvider {

    private final ScriptEngineService scriptEngine;

    private final DynamicByteArray responseBody;
    private final DynamicByteArray errorResponseBody;

    public BodyProviderStreamAlways(ScriptEngineService scriptEngine, DynamicByteArray responseBody, DynamicByteArray errorResponseBody) {
        this.responseBody = responseBody;
        this.scriptEngine = scriptEngine;
        this.errorResponseBody = errorResponseBody;
    }

    @Override
    public Publisher<byte[]> from(HttpServerResponse response, Message message, FlowContext flowContext) {
        return scriptEngine.evaluateStream(responseBody, flowContext, message);
    }

    @Override
    public Publisher<byte[]> from(HttpServerResponse response, Throwable throwable, FlowContext flowContext) {
        try {
            return scriptEngine.evaluateStream(errorResponseBody, flowContext, throwable);
        } catch (Exception exception) {
            // Evaluating an error response, cannot throw again an exception,
            // Therefore we catch any exception and we return the exception message.
            return StackTraceUtils.asByteStream(exception);
        }
    }
}
