package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.DefaultMessageAttributes;

import java.io.Serializable;
import java.util.Map;

public class HttpRequestAttributes extends DefaultMessageAttributes {
    public HttpRequestAttributes(Map<String, Serializable> attributes) {
        super(attributes);
    }
}
