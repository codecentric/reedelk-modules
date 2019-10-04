package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsDouble extends BaseConverter<Integer,Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(Integer value) {
        return value == null ? null : value.doubleValue();
    }
}
