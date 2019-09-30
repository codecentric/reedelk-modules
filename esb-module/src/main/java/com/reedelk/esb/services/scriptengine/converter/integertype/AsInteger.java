package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;
import org.reactivestreams.Publisher;

public class AsInteger implements DynamicValueConverter<Integer,Integer> {

    @Override
    public Integer from(Integer value) {
        return value;
    }

    @Override
    public Publisher<Integer> from(Publisher<Integer> stream) {
        return stream;
    }
}
