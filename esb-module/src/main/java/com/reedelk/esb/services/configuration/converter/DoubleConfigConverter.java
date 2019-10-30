package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.exception.ConfigPropertyException;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DoubleConfigConverter implements ConfigConverter<Double> {

    @Override
    public Double convert(ConfigurationService configurationService, String pid, String key, Double defaultValue) {
        return configurationService.getDoubleFrom(pid, key, defaultValue);
    }

    @Override
    public Double convert(ConfigurationService configurationService, String pid, String key) throws ConfigPropertyException {
        return configurationService.getDoubleFrom(pid, key);
    }
}
