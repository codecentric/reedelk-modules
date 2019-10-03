package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsInteger implements DynamicValueConverter<Double,Integer> {

    @Override
    public Integer from(Double value) {
        return value == null ? null : value.intValue();
    }

    @Override
    public TypedPublisher<Integer> from(TypedPublisher<Double> stream) {
        return TypedPublisher.from(Flux.from(stream).map(this::from), Integer.class);
    }
}
