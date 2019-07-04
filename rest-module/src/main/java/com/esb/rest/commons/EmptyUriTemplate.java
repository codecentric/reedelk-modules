package com.esb.rest.commons;

import java.util.HashMap;
import java.util.Map;

public class EmptyUriTemplate extends UriTemplate {

    @Override
    public boolean matches(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> bind(String uri) {
        return new HashMap<>();
    }
}
