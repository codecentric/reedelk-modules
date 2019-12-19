package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.commons.ConfigurationPropertyUtils;
import com.reedelk.runtime.api.configuration.ConfigurationService;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigPropertyDecorator implements TypeFactory {

    private final TypeFactory delegate;
    private final ConfigurationService configurationService;

    public ConfigPropertyDecorator(ConfigurationService configurationService, TypeFactory delegate) {
        this.configurationService = configurationService;
        this.delegate = delegate;
    }

    @Override
    public boolean isPrimitive(Class<?> clazz) {
        return delegate.isPrimitive(clazz);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName, TypeFactoryContext context) {
        // Note that a component definition might be null for some types. For instance, the ModuleId type
        // does not require any component definition in order to be instantiated, it only requires the moduleId.
        if (jsonObject != null ) {

            Object propertyValue = jsonObject.get(propertyName);

            // If component definition value is a string and starts with $[],
            // then it is a system property.
            if (ConfigurationPropertyUtils.isConfigProperty(propertyValue)) {
                String propertyKey = ConfigurationPropertyUtils.unwrap((String) propertyValue);
                return configurationService.get(propertyKey, expectedClass);
            }
        }

        // Otherwise we use the default converter.
        return delegate.create(expectedClass, jsonObject, propertyName, context);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index, TypeFactoryContext context) {
        Object value = jsonArray.get(index);
        // If the array value is a string and starts with $[],
        // then it is a system property, otherwise we just use the default value.
        if (ConfigurationPropertyUtils.isConfigProperty(value)) {
            String propertyKey = ConfigurationPropertyUtils.unwrap((String) value);
            return configurationService.get(propertyKey, expectedClass);
        }

        // Otherwise we use the default converter.
        return delegate.create(expectedClass, jsonArray, index, context);
    }
}
