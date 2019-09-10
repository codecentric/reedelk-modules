package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.AbstractMessageAttributes;

public class HttpRequestAttributes extends AbstractMessageAttributes {

    @Override
    public String toString() {
        return HttpRequestAttributes.class.getSimpleName() + super.toString();
    }
}
