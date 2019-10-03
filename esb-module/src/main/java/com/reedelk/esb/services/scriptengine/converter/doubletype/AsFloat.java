package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsFloat implements DynamicValueConverter<Double,Float> {

    @Override
    public Float from(Double value) {
        if (value == null) {
            return null;
        } else {
            return value.floatValue();
        }
    }

    @Override
    public TypedPublisher<Float> from(TypedPublisher<Double> stream) {
        return TypedPublisher.from(Flux.from(stream).map(this::from), Float.class);
    }
}
