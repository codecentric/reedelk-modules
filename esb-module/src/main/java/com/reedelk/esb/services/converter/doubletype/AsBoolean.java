package com.reedelk.esb.services.converter.doubletype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsBoolean extends BaseConverter<Double,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(Double value) {
        return value == 1d;
    }
}
