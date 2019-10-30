package com.reedelk.esb.services.converter.bytearraytype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsString extends BaseConverter<byte[],String> {

    public AsString() {
        super(String.class);
    }

    @Override
    public String from(byte[] value) {
        return new String(value);
    }
}
