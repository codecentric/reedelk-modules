package com.reedelk.esb.services.scriptengine.converter.bytearraytype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Converters {

    public static final Map<Class<?>, ValueConverter<?,?>> ALL;
    static {
        Map<Class<?>, ValueConverter<?, ?>> tmp = new HashMap<>();
        tmp.put(byte[].class, new AsByteArray());
        ALL = Collections.unmodifiableMap(tmp);
    }

    private Converters() {
    }
}
