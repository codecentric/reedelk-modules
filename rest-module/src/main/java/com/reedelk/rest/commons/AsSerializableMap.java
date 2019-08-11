package com.reedelk.rest.commons;

import java.util.HashMap;
import java.util.Map;

public class AsSerializableMap {

    public static HashMap<String, ?> of(Map<String, ?> original) {
        return original == null ?
                new HashMap<>() :
                new HashMap<>(original);
    }
}
