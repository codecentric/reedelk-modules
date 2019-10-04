package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsBoolean extends BaseConverter<Integer,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(Integer value) {
        return value == null ? Boolean.FALSE : value == 1;
    }
}
