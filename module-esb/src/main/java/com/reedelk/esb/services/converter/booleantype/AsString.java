package com.reedelk.esb.services.converter.booleantype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsString extends BaseConverter<Boolean,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Boolean value) {
        return String.valueOf(value);
    }
}
