package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.exception.ConfigPropertyException;
import com.reedelk.runtime.api.service.ConfigurationService;

public class FloatConfigConverter implements ConfigConverter<Float> {

    @Override
    public Float convert(ConfigurationService configurationService, String pid, String key, Float defaultValue) {
        return configurationService.getFloatFrom(pid, key, defaultValue);
    }

    @Override
    public Float convert(ConfigurationService configurationService, String pid, String key) throws ConfigPropertyException {
        return configurationService.getFloatFrom(pid, key);
    }
}
