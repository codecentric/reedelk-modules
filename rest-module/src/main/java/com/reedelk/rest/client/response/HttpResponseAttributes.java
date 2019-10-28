package com.reedelk.rest.client.response;

import com.reedelk.runtime.api.message.DefaultMessageAttributes;

import java.io.Serializable;
import java.util.Map;

public class HttpResponseAttributes extends DefaultMessageAttributes {
    public HttpResponseAttributes(Map<String, Serializable> attributes) {
        super(attributes);
    }
}
