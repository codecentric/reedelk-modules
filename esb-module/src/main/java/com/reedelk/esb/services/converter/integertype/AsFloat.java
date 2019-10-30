package com.reedelk.esb.services.converter.integertype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsFloat extends BaseConverter<Integer,Float> {

    AsFloat() {
        super(Float.class);
    }

    @Override
    public Float from(Integer value) {
        return value.floatValue();
    }
}
