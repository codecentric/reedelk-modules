package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.evaluator.DynamicValueConverter;

public class AsInteger implements DynamicValueConverter<Double,Integer> {
    @Override
    public Integer to(Double value) {
        return value.intValue();
    }
}
