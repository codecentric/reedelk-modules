package com.reedelk.esb.services.scriptengine.converter.defaulttype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;
import com.reedelk.runtime.api.commons.ObjectToBytes;

class AsByteArray extends BaseConverter<Object,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Object value) {
        return ObjectToBytes.from(value);
    }
}
