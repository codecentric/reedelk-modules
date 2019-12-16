package com.reedelk.esb.services.converter.booleantype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsFloat extends BaseConverter<Boolean,Float> {

    AsFloat() {
        super(Float.class);
    }

    @Override
    public Float from(Boolean value) {
        return value == Boolean.TRUE ? 1f : 0f;
    }
}
