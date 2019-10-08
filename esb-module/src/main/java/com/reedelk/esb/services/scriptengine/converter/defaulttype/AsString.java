package com.reedelk.esb.services.scriptengine.converter.defaulttype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsString extends BaseConverter<Object,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Object value) {
        return value == null ? null : value.toString();
    }
}
