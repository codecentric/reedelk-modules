package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Converters {

    public static final Map<Class<?>, DynamicValueConverter<?, ?>> ALL;
    static {
        Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
        tmp.put(Integer.class, new com.reedelk.esb.services.scriptengine.converter.doubletype.AsInteger());
        ALL = Collections.unmodifiableMap(tmp);
    }

    private Converters() {
    }
}
