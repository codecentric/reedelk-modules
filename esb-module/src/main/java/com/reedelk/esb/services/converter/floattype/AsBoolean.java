package com.reedelk.esb.services.converter.floattype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsBoolean extends BaseConverter<Float,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(Float value) {
        return value == 1f;
    }
}
