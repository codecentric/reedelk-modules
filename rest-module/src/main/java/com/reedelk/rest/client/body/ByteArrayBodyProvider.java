package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;

public class ByteArrayBodyProvider extends AbstractBodyProvider<byte[]> implements BodyProvider {

    private static final byte[] EMPTY = new byte[0];

    ByteArrayBodyProvider(ScriptEngineService scriptEngine, String body) {
        super(scriptEngine, body);
    }

    @Override
    public byte[] asByteArray(Message message, FlowContext flowContext) {
        return from(message, flowContext);
    }

    @Override
    protected byte[] fromContent(TypedContent<?> content) {
        return content.asByteArray();
    }

    @Override
    protected byte[] fromBytes(byte[] bytes) {
        return bytes;
    }

    @Override
    protected byte[] empty() {
        return EMPTY;
    }
}
