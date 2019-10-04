package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsBoolean extends BaseConverter<Double,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(Double value) {
        return value == null ? Boolean.FALSE : value == 1d;
    }
}
