package com.reedelk.esb.services.converter.defaulttype;

import com.reedelk.esb.services.converter.BaseConverter;
import com.reedelk.runtime.commons.ObjectToBytes;

class AsByteArray extends BaseConverter<Object,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Object value) {
        return ObjectToBytes.from(value);
    }
}
