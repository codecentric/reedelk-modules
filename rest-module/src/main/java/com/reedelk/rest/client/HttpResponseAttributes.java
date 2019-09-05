package com.reedelk.rest.client;

import com.reedelk.runtime.api.message.AbstractMessageAttributes;

public class HttpResponseAttributes extends AbstractMessageAttributes {
    @Override
    public String toString() {
        return HttpResponseAttributes.class.getSimpleName() + super.toString();
    }
}
