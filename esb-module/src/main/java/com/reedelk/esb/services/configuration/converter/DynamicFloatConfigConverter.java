package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicFloat;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DynamicFloatConfigConverter implements ConfigConverter<DynamicFloat> {

    private final FloatConfigConverter delegate = new FloatConfigConverter();

    @Override
    public DynamicFloat convert(ConfigurationService configurationService, String pid, String key, DynamicFloat defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicFloat convert(ConfigurationService configurationService, String pid, String key) {
        float configValue = delegate.convert(configurationService, pid, key);
        return DynamicFloat.from(configValue);
    }
}
