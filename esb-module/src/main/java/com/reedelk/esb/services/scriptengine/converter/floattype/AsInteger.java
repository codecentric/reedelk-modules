package com.reedelk.esb.services.scriptengine.converter.floattype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsInteger extends BaseConverter<Float,Integer> {

    AsInteger() {
        super(Integer.class);
    }

    @Override
    public Integer from(Float value) {
        return value == null ? null : value.intValue();
    }
}
