package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.DynamicValueConverter;

public class AsString implements DynamicValueConverter<Integer,String> {
    @Override
    public String to(Integer value) {
        return value == null ? null : value.toString();
    }
}
