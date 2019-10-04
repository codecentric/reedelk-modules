package com.reedelk.esb.services.scriptengine.converter.floattype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsString extends BaseConverter<Float,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Float value) {
        return value == null ? null : String.valueOf(value);
    }
}
