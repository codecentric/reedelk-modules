package com.reedelk.rest.client.body;

import com.reedelk.rest.commons.IsMessagePayload;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

public class DefaultBodyProvider implements BodyProvider {

    private static final byte[] EMPTY = new byte[0];

    private final DynamicByteArray body;
    private final ScriptEngineService scriptEngine;

    DefaultBodyProvider(ScriptEngineService scriptEngine, DynamicByteArray body) {
        this.body = body;
        this.scriptEngine = scriptEngine;
    }

    @Override
    public byte[] asByteArray(Message message, FlowContext flowContext) {
        return scriptEngine.evaluate(body, message, flowContext).orElse(EMPTY);
    }

    @Override
    public Publisher<byte[]> asStream(Message message, FlowContext flowContext) {
        return scriptEngine.evaluateStream(body, message, flowContext);
    }

    @Override
    public boolean streamable(Message message) {
        if (IsMessagePayload.from(body)) {
            return message.getContent().isStream() &&
                    !message.getContent().isConsumed();
        }
        return false;
    }
}
