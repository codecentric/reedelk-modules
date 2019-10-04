package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsBoolean extends BaseConverter<String,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(String value) {
        return Boolean.parseBoolean(value);
    }
}
