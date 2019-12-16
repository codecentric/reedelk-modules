package com.reedelk.esb.services.converter.integertype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsBoolean extends BaseConverter<Integer,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(Integer value) {
        return value == 1;
    }
}
