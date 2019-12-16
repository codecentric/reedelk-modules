package com.reedelk.esb.services.converter.floattype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsByteArray extends BaseConverter<Float,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Float value) {
        return new byte[] {value.byteValue()};
    }
}
