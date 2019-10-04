package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsInteger implements ValueConverter<String,Integer> {

    @Override
    public Integer from(String value) {
        return Integer.valueOf(value);
    }

    @Override
    public TypedPublisher<Integer> from(TypedPublisher<String> stream) {
        return TypedPublisher.fromInteger(Flux.from(stream).map(Integer::valueOf));
    }
}
