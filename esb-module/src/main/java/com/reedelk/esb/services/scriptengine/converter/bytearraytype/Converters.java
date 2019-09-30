package com.reedelk.esb.services.scriptengine.converter.bytearraytype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;

import java.util.HashMap;
import java.util.Map;

public class Converters {

    public static final Map<Class<?>, DynamicValueConverter<?,?>> ALL;
    static {
        Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
        tmp.put(byte[].class, new AsByteArray());
        ALL = tmp;
    }
}
