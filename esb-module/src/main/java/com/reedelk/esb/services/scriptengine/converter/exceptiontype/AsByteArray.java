package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;

public class AsByteArray extends BaseConverter<Exception,byte[]> {

    AsByteArray() {
        super(byte[].class);
    }

    @Override
    public byte[] from(Exception value) {
        return StackTraceUtils.asByteArray(value);
    }
}
