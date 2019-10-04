package com.reedelk.esb.services.scriptengine.converter.booleantype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsString implements ValueConverter<Boolean,String> {

    @Override
    public String from(Boolean value) {
        return value == null ? null  : value.toString();
    }

    @Override
    public TypedPublisher<String> from(TypedPublisher<Boolean> stream) {
        return TypedPublisher.fromString(Flux.from(stream).map(this::from));
    }
}
