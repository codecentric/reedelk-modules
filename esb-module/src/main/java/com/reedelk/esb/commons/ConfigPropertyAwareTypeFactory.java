package com.reedelk.esb.commons;

import com.reedelk.runtime.api.commons.ConfigurationPropertyUtils;
import com.reedelk.runtime.api.service.ConfigurationService;
import com.reedelk.runtime.commons.TypeFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigPropertyAwareTypeFactory {

    private ConfigurationService configurationService;

    public ConfigPropertyAwareTypeFactory(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public Object create(Class<?> clazz, JSONObject componentDefinition, String propertyName, long moduleId) {
        // If component definition value is a string and starts with $[], then it is a system property.
        // Otherwise we use the default converter.
        Object propertyValue = componentDefinition.get(propertyName);
        if (ConfigurationPropertyUtils.isConfigProperty(propertyValue)) {
            String propertyKey = ConfigurationPropertyUtils.unwrap((String) propertyValue);
            return configurationService.get(propertyKey, clazz);
        } else {
            return TypeFactory.create(clazz, componentDefinition, propertyName, moduleId);
        }
    }

    public Object create(Class<?> genericType, JSONArray array, int index) {
        // If component definition is a string and starts with $[],
        // then it is a system property. Otherwise we use the default converter.
        return TypeFactory.create(genericType, array, index);
    }
}
