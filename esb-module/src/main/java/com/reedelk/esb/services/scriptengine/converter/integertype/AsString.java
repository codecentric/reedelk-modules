package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsString implements DynamicValueConverter<Integer,String> {

    @Override
    public String from(Integer value) {
        return value == null ? null : value.toString();
    }

    @Override
    public TypedPublisher<String> from(TypedPublisher<Integer> stream) {
        return TypedPublisher.from(Flux.from(stream).map(String::valueOf), String.class);
    }
}
