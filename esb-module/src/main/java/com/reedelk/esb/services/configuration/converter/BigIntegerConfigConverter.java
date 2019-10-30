package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.exception.ConfigPropertyException;
import com.reedelk.runtime.api.service.ConfigurationService;

import java.math.BigInteger;

public class BigIntegerConfigConverter implements ConfigConverter<BigInteger> {

    @Override
    public BigInteger convert(ConfigurationService configurationService, String pid, String key, BigInteger defaultValue) {
        return configurationService.getBigIntegerFrom(pid, key, defaultValue);
    }

    @Override
    public BigInteger convert(ConfigurationService configurationService, String pid, String key) throws ConfigPropertyException {
        return configurationService.getBigIntegerFrom(pid, key);
    }
}
