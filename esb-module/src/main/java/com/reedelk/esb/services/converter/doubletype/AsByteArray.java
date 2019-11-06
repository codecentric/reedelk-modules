package com.reedelk.esb.services.converter.doubletype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsByteArray extends BaseConverter<Double,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Double value) {
        return new byte[] {value.byteValue()};
    }
}