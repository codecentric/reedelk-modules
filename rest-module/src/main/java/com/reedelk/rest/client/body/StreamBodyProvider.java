package com.reedelk.rest.client.body;

import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class StreamBodyProvider extends AbstractBodyProvider<Publisher<byte[]>> {

    StreamBodyProvider(ScriptEngineService scriptEngine, String body) {
        super(scriptEngine, body);
    }

    @Override
    protected Publisher<byte[]> empty() {
        return Mono.empty();
    }

    @Override
    protected Publisher<byte[]> fromBytes(byte[] bytes) {
        return Mono.just(bytes);
    }

    @Override
    protected Publisher<byte[]> fromContent(TypedContent<?> content) {
        return content.asByteArrayStream();
    }
}
