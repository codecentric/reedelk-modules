package com.reedelk.esb.services.converter.stringtype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsFloat extends BaseConverter<String,Float> {

    AsFloat() {
        super(Float.class);
    }

    @Override
    public Float from(String value) {
        return Float.parseFloat(value);
    }
}
