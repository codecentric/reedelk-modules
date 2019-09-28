package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;

public class AsInteger implements DynamicValueConverter<String,Integer> {
    @Override
    public Integer to(String value) {
        return Integer.valueOf(value);
    }
}
