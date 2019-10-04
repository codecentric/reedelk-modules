package com.reedelk.esb.services.scriptengine.converter.floattype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsByteArray extends BaseConverter<Float,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Float value) {
        return value == null ? new byte[0] : new byte[] {value.byteValue()};
    }
}
