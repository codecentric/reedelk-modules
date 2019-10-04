package com.reedelk.esb.services.scriptengine.converter.booleantype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsString extends BaseConverter<Boolean,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Boolean value) {
        return value == null ? null  : value.toString();
    }
}
