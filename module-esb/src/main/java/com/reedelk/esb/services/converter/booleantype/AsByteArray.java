package com.reedelk.esb.services.converter.booleantype;

import com.reedelk.esb.services.converter.BaseConverter;

class AsByteArray extends BaseConverter<Boolean,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Boolean value) {
        byte byteVal = (byte) (value == Boolean.TRUE ? 1 : 0);
        return new byte[]{byteVal};
    }
}