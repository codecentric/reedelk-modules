package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import org.reactivestreams.Publisher;
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
    public Publisher<Float> from(Publisher<Double> stream) {
        return Flux.from(stream).map(this::from);
    }
}
