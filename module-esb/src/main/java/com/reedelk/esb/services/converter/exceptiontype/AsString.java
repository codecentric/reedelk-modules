package com.reedelk.esb.services.converter.exceptiontype;

import com.reedelk.esb.services.converter.BaseConverter;
import com.reedelk.runtime.api.commons.StackTraceUtils;

public class AsString extends BaseConverter<Exception,String> {

    AsString() {
        super(String.class);
    }

    @Override
    public String from(Exception value) {
        return StackTraceUtils.asString(value);
    }

}
