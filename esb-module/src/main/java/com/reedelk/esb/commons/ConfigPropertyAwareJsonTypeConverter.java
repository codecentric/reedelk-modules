package com.reedelk.esb.commons;

import com.reedelk.runtime.api.commons.ConfigurationPropertyUtils;
import com.reedelk.runtime.api.service.ConfigurationService;
import com.reedelk.runtime.commons.JsonTypeConverter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigPropertyAwareJsonTypeConverter {

    private ConfigurationService configurationService;

    public ConfigPropertyAwareJsonTypeConverter(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public Object convert(Class<?> clazz, JSONObject componentDefinition, String propertyName) {
        // If component definition value is a string and starts with $[], then it is a system property.
        // Otherwise we use the default converter.
        Object propertyValue = componentDefinition.get(propertyName);
        if (ConfigurationPropertyUtils.isConfigProperty(propertyValue)) {
            String propertyKey = ConfigurationPropertyUtils.unwrap((String) propertyValue);
            return configurationService.get(propertyKey, clazz);
        } else {
            return JsonTypeConverter.convert(clazz, componentDefinition, propertyName);
        }
    }

    public Object convert(Class<?> genericType, JSONArray array, int index) {
        // If component definition is a string and starts with $[],
        // then it is a system property. Otherwise we use the default converter.
        return JsonTypeConverter.convert(genericType, array, index);
    }
}
