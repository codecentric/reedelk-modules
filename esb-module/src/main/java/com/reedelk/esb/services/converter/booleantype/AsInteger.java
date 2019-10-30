package com.reedelk.esb.services.converter.booleantype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsInteger extends BaseConverter<Boolean, Integer> {

    AsInteger() {
        super(Integer.class);
    }

    @Override
    public Integer from(Boolean value) {
        return value == Boolean.TRUE ? 1 : 0;
    }
}
