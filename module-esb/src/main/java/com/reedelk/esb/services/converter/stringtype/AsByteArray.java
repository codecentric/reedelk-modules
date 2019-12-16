package com.reedelk.esb.services.converter.stringtype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsByteArray extends BaseConverter<String,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(String value) {
        return value.getBytes();
    }
}
