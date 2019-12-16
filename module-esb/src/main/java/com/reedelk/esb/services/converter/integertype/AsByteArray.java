package com.reedelk.esb.services.converter.integertype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsByteArray extends BaseConverter<Integer,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Integer value) {
        return new byte[] {value.byteValue()};
    }
}
