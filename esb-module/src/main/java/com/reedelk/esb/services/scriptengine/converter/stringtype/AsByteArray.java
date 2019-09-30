package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public class AsByteArray implements DynamicValueConverter<String,byte[]> {

    @Override
    public byte[] from(String value) {
        return value == null ?
                new byte[0] :
                value.getBytes();
    }

    @Override
    public Publisher<byte[]> from(Publisher<String> stream) {
        return Flux.from(stream).map(new Function<String, byte[]>() {
            @Override
            public byte[] apply(String input) {
                return from(input);
            }
        });
    }
}
