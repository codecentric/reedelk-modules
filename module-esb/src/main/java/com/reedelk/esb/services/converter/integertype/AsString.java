package com.reedelk.esb.services.converter.integertype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsString extends BaseConverter<Integer,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Integer value) {
        return String.valueOf(value);
    }
}
