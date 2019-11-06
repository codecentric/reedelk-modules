package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicBigInteger;
import com.reedelk.runtime.api.service.ConfigurationService;

import java.math.BigInteger;

public class DynamicBigIntegerConfigConverter implements ConfigConverter<DynamicBigInteger> {

    private final BigIntegerConfigConverter delegate = new BigIntegerConfigConverter();

    @Override
    public DynamicBigInteger convert(ConfigurationService configurationService, String pid, String key, DynamicBigInteger defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicBigInteger convert(ConfigurationService configurationService, String pid, String key) {
        BigInteger configValue = delegate.convert(configurationService, pid, key);
        return DynamicBigInteger.from(configValue);
    }
}