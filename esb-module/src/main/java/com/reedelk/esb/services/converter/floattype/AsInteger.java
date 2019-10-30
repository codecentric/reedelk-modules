package com.reedelk.esb.services.converter.floattype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsInteger extends BaseConverter<Float,Integer> {

    AsInteger() {
        super(Integer.class);
    }

    @Override
    public Integer from(Float value) {
        return value.intValue();
    }
}
