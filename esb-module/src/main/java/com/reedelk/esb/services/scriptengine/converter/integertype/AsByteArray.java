package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsByteArray extends BaseConverter<Integer,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Integer value) {
        return new byte[] {value.byteValue()};
    }
}
