package com.reedelk.esb.services.scriptengine.converter.bytearraytype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

public class AsString extends BaseConverter<byte[],String> {

    public AsString() {
        super(String.class);
    }

    @Override
    public String from(byte[] value) {
        return new String(value);
    }
}
