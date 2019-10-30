package com.reedelk.esb.services.converter.floattype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsDouble extends BaseConverter<Float,Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(Float value) {
        return value.doubleValue();
    }
}
