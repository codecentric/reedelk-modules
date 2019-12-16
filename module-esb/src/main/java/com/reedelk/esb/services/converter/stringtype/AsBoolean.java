package com.reedelk.esb.services.converter.stringtype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsBoolean extends BaseConverter<String,Boolean> {

    AsBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean from(String value) {
        return Boolean.parseBoolean(value);
    }
}
