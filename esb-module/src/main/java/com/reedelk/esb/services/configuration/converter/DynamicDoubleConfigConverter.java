package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicDouble;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DynamicDoubleConfigConverter implements ConfigConverter<DynamicDouble> {

    private final DoubleConfigConverter delegate = new DoubleConfigConverter();

    @Override
    public DynamicDouble convert(ConfigurationService configurationService, String pid, String key, DynamicDouble defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicDouble convert(ConfigurationService configurationService, String pid, String key) {
        double configValue = delegate.convert(configurationService, pid, key);
        return DynamicDouble.from(configValue);
    }
}
