package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsByteArray extends BaseConverter<Double,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Double value) {
        return value == null ? new byte[0] : new byte[] {value.byteValue()};
    }
}
