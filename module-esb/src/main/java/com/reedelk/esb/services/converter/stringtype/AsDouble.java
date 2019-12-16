package com.reedelk.esb.services.converter.stringtype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsDouble extends BaseConverter<String,Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(String value) {
        return Double.parseDouble(value);
    }
}
