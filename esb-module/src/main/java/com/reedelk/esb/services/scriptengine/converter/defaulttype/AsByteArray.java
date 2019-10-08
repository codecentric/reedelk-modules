package com.reedelk.esb.services.scriptengine.converter.defaulttype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsByteArray extends BaseConverter<Object,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Object value) {
        return value == null ? new  byte[0] : value.toString().getBytes();
    }
}
