package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsByteArray extends BaseConverter<String,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(String value) {
        return value == null ? new byte[0] : value.getBytes();
    }
}
