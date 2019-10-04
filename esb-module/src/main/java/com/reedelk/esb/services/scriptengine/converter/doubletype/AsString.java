package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsString extends BaseConverter<Double,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Double value) {
        return String.valueOf(value);
    }
}
