package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicBigDecimal;
import com.reedelk.runtime.api.service.ConfigurationService;

import java.math.BigDecimal;

public class DynamicBigDecimalConfigConverter implements ConfigConverter<DynamicBigDecimal> {

    private final BigDecimalConfigConverter delegate = new BigDecimalConfigConverter();

    @Override
    public DynamicBigDecimal convert(ConfigurationService configurationService, String pid, String key, DynamicBigDecimal defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicBigDecimal convert(ConfigurationService configurationService, String pid, String key) {
        BigDecimal configValue = delegate.convert(configurationService, pid, key);
        return DynamicBigDecimal.from(configValue);
    }
}
