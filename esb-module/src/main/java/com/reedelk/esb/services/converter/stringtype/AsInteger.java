package com.reedelk.esb.services.converter.stringtype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsInteger extends BaseConverter<String,Integer> {

    AsInteger() {
        super(Integer.class);
    }

    @Override
    public Integer from(String value) {
        return Integer.valueOf(value);
    }

}
