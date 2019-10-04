package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsFloat extends BaseConverter<Double,Float> {

    AsFloat() {
        super(Float.class);
    }

    @Override
    public Float from(Double value) {
        return value.floatValue();
    }
}
