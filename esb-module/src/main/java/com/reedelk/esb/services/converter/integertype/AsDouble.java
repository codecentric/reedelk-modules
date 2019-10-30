package com.reedelk.esb.services.converter.integertype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsDouble extends BaseConverter<Integer,Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(Integer value) {
        return value.doubleValue();
    }
}
