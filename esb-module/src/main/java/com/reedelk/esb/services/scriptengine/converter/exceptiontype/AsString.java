package com.reedelk.esb.services.scriptengine.converter.exceptiontype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;
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
