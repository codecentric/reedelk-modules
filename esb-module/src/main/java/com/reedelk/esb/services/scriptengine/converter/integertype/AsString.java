package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsString extends BaseConverter<Integer,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Integer value) {
        return value == null ? null : String.valueOf(value);
    }
}
