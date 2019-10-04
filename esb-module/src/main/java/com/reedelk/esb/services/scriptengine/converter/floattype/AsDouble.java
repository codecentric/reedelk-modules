package com.reedelk.esb.services.scriptengine.converter.floattype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsDouble extends BaseConverter<Float,Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(Float value) {
        return value == null ? null : value.doubleValue();
    }
}
