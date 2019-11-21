package com.reedelk.rest.client.body;

import com.reedelk.rest.commons.IsEvaluateMessagePayload;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

public class DefaultBodyProvider implements BodyProvider {

    private final DynamicByteArray body;
    private final ScriptEngineService scriptEngine;
    private final boolean isEvaluateMessagePayloadBody;

    DefaultBodyProvider(ScriptEngineService scriptEngine, DynamicByteArray body) {
        this.body = body;
        this.scriptEngine = scriptEngine;
        this.isEvaluateMessagePayloadBody = IsEvaluateMessagePayload.from(body);
    }

    @Override
    public byte[] asByteArray(Message message, FlowContext flowContext) {
        return scriptEngine.evaluate(body, flowContext, message).orElse(new byte[0]);
    }

    @Override
    public Publisher<byte[]> asStream(Message message, FlowContext flowContext) {
        return scriptEngine.evaluateStream(body, flowContext, message);
    }

    @Override
    public boolean streamable(Message message) {
        if (isEvaluateMessagePayloadBody) {
            return message.getContent().isStream() &&
                    !message.getContent().isConsumed();
        }
        return false;
    }
}
