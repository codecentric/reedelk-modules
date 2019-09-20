package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class StreamBodyProvider extends AbstractBodyProvider<Publisher<byte[]>> implements BodyProvider {

    StreamBodyProvider(ScriptEngineService scriptEngine, String body) {
        super(scriptEngine, body);
    }

    @Override
    public Publisher<byte[]> asStream(Message message, FlowContext flowContext) {
        return from(message, flowContext);
    }

    @Override
    protected Publisher<byte[]> fromContent(TypedContent<?> content) {
        return content.asByteArrayStream();
    }

    @Override
    protected Publisher<byte[]> fromBytes(byte[] bytes) {
        return Mono.just(bytes);
    }

    @Override
    protected Publisher<byte[]> empty() {
        return Mono.empty();
    }
}
