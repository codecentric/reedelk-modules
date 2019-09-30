package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.converter.DynamicValueConverter;

import java.util.HashMap;
import java.util.Map;

public class Converters {

    public static final Map<Class<?>, DynamicValueConverter<?, ?>> ALL;
    static {
        Map<Class<?>, DynamicValueConverter<?, ?>> tmp = new HashMap<>();
        tmp.put(String.class, new AsString());
        tmp.put(Integer.class, new AsInteger());
        tmp.put(Boolean.class, new AsBoolean());
        tmp.put(byte[].class, new AsByteArray());
        ALL = tmp;
    }
}
