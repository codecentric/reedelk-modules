package com.reedelk.esb.services.converter.doubletype;

import com.reedelk.esb.services.converter.BaseConverter;

public class AsInteger extends BaseConverter<Double,Integer> {

    AsInteger() {
        super(Integer.class);
    }

    @Override
    public Integer from(Double value) {
        return  value.intValue();
    }
}
