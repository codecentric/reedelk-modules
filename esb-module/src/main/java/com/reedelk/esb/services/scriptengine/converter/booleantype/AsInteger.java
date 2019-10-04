package com.reedelk.esb.services.scriptengine.converter.booleantype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsInteger extends BaseConverter<Boolean, Integer> {

    public AsInteger() {
        super(Integer.class);
    }

    @Override
    public Integer from(Boolean value) {
        return value == Boolean.TRUE ? 1 : 0;
    }
}
