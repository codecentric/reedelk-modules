package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsBoolean implements DynamicValueConverter<String,Boolean> {

    @Override
    public Boolean from(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public TypedPublisher<Boolean> from(TypedPublisher<String> stream) {
        return TypedPublisher.from(Flux.from(stream).map(this::from), boolean.class);
    }
}
