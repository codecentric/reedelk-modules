package com.reedelk.esb.services.converter.defaulttype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsString extends BaseConverter<Object,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Object value) {
        return value == null ? null : value.toString();
    }
}
