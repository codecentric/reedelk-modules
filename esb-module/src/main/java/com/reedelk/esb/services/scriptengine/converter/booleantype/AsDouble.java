package com.reedelk.esb.services.scriptengine.converter.booleantype;

import com.reedelk.esb.services.scriptengine.converter.BaseConverter;

class AsDouble extends BaseConverter<Boolean, Double> {

    AsDouble() {
        super(Double.class);
    }

    @Override
    public Double from(Boolean value) {
        return value == Boolean.TRUE ? 1d : 0d;
    }
}
