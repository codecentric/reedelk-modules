package com.reedelk.esb.services.converter.floattype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsString extends BaseConverter<Float,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Float value) {
        return String.valueOf(value);
    }
}
