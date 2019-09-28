package com.reedelk.esb.services.scriptengine.converter.stringtype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;

public class AsString implements DynamicValueConverter<String,String> {
    @Override
    public String to(String value) {
        return value;
    }
}
