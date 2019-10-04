package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsFloat implements ValueConverter<Double,Float> {

    @Override
    public Float from(Double value) {
        return value == null ? null : value.floatValue();
    }

    @Override
    public TypedPublisher<Float> from(TypedPublisher<Double> stream) {
        return TypedPublisher.fromFloat(Flux.from(stream).map(this::from));
    }
}
