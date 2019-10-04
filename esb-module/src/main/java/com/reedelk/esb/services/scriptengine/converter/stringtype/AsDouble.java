package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsDouble extends BaseConverter<String,Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(String value) {
        return Double.parseDouble(value);
    }
}
