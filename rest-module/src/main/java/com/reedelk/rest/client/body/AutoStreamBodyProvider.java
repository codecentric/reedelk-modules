package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

public class AutoStreamBodyProvider implements BodyProvider {

    private final ByteArrayBodyProvider byteArrayBodyProvider;
    private final StreamBodyProvider streamBodyProvider;
    private final String body;

    AutoStreamBodyProvider(ScriptEngineService scriptEngine, String body) {
        this.body = body;
        this.byteArrayBodyProvider = new ByteArrayBodyProvider(scriptEngine, body);
        this.streamBodyProvider = new StreamBodyProvider(scriptEngine, body);
    }

    @Override
    public byte[] asByteArray(Message message, FlowContext flowContext) {
        return byteArrayBodyProvider.asByteArray(message, flowContext);
    }

    @Override
    public Publisher<byte[]> asStream(Message message, FlowContext flowContext) {
        return streamBodyProvider.asStream(message, flowContext);
    }

    /**
     * A message payload is streamable if and only if it is a stream
     * and its content has not been consumed already. If it has been
     * consumed already it means that it is fully loaded in memory,
     * hence we can send all bytes in one shot and the payload length
     * is therefore known.
     * @param message the input message.
     * @return true if the body is a script to send the message payload
     *  and the message payload is not a consumed stream, false otherwise.
     */
    @Override
    public boolean streamable(Message message) {
        if (ScriptUtils.isMessagePayload(body)) {
            return message.getContent().isStream() &&
                    !message.getContent().isConsumed();
        }
        return false;
    }
}
