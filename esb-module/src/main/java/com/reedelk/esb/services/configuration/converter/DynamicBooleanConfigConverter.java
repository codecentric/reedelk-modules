package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicBoolean;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DynamicBooleanConfigConverter implements ConfigConverter<DynamicBoolean> {

    private final BooleanConfigConverter delegate = new BooleanConfigConverter();

    @Override
    public DynamicBoolean convert(ConfigurationService configurationService, String pid, String key, DynamicBoolean defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicBoolean convert(ConfigurationService configurationService, String pid, String key) {
        boolean configValue = delegate.convert(configurationService, pid, key);
        return DynamicBoolean.from(configValue);
    }
}
