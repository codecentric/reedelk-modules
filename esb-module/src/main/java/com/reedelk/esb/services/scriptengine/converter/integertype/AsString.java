package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsString implements ValueConverter<Integer,String> {

    @Override
    public String from(Integer value) {
        return value == null ? null : value.toString();
    }

    @Override
    public TypedPublisher<String> from(TypedPublisher<Integer> stream) {
        return TypedPublisher.fromString(Flux.from(stream).map(String::valueOf));
    }
}
