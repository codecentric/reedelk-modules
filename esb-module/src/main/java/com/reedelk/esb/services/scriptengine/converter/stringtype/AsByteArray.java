package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsByteArray implements ValueConverter<String,byte[]> {

    @Override
    public byte[] from(String value) {
        return value == null ?
                new byte[0] :
                value.getBytes();
    }

    @Override
    public TypedPublisher<byte[]> from(TypedPublisher<String> stream) {
        return TypedPublisher.fromByteArray(Flux.from(stream).map(this::from));
    }
}
