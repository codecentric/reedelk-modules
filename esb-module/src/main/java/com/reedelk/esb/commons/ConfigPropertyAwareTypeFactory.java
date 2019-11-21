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
        // Note that a component definition might be null for some types. For instance, the ModuleId type
        // does not require any component definition in order to be instantiated, it only requires the moduleId.
        if (componentDefinition != null ) {

            Object propertyValue = componentDefinition.get(propertyName);

            // If component definition value is a string and starts with $[], then it is a system property.
            if (ConfigurationPropertyUtils.isConfigProperty(propertyValue)) {
                String propertyKey = ConfigurationPropertyUtils.unwrap((String) propertyValue);
                return configurationService.get(propertyKey, clazz);
            }
        }

        // Otherwise we use the default converter.
        return TypeFactory.create(clazz, componentDefinition, propertyName, moduleId);
    }

    public Object create(Class<?> genericType, JSONArray array, int index, long moduleId) {
        // If component definition is a string and starts with $[],
        // then it is a system property. Otherwise we use the default converter.
        return TypeFactory.create(genericType, array, index, moduleId);
    }
}
