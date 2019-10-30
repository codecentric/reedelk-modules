package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DynamicStringConfigConverter implements ConfigConverter<DynamicString> {

    private final StringConfigConverter delegate = new StringConfigConverter();

    @Override
    public DynamicString convert(ConfigurationService configurationService, String pid, String key, DynamicString defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicString convert(ConfigurationService configurationService, String pid, String key) {
        String configValue = delegate.convert(configurationService, pid, key);
        return DynamicString.from(configValue);
    }
}
