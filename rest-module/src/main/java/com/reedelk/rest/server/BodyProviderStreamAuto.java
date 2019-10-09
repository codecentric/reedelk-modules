package com.reedelk.rest.server;

import com.reedelk.rest.commons.IsEvaluateMessagePayload;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerResponse;

public class BodyProviderStreamAuto implements BodyProvider {


    private final boolean isEvaluateMessagePayload;

    private BodyProviderStreamNone streamNone;
    private BodyProviderStreamAlways streamAlways;

    BodyProviderStreamAuto(ScriptEngineService scriptEngine,
                           DynamicByteArray responseBody,
                           DynamicByteArray errorResponseBody) {
        isEvaluateMessagePayload = IsEvaluateMessagePayload.from(responseBody);
        streamNone = new BodyProviderStreamNone(scriptEngine, responseBody, errorResponseBody);
        streamAlways = new BodyProviderStreamAlways(scriptEngine, responseBody, errorResponseBody);
    }

    @Override
    public Publisher<byte[]> from(HttpServerResponse response, Message message, FlowContext flowContext) {
        if (isEvaluateMessagePayload) {
            if (isMessageStreamable(message)) {
                return streamAlways.from(response, message, flowContext);
            }
        }
        return streamNone.from(response, message, flowContext);
    }

    @Override
    public Publisher<byte[]> from(HttpServerResponse response, Throwable throwable, FlowContext flowContext) {
        // An exception is never streamed.
        return streamNone.from(response, throwable, flowContext);
    }

    private boolean isMessageStreamable(Message message) {
        return message.getContent().isStream() &&
                !message.getContent().isConsumed();
    }
}
