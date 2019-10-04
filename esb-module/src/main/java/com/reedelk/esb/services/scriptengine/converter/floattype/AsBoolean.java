package com.reedelk.esb.services.scriptengine.converter.floattype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsBoolean extends BaseConverter<Float,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(Float value) {
        return value == null ? Boolean.FALSE : value == 1f;
    }
}
