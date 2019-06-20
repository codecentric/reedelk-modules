package com.esb.module.deserializer;

import com.esb.internal.commons.FileUtils;
import com.esb.internal.commons.JsonParser;
import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;
import org.json.JSONObject;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.esb.internal.commons.FileExtension.*;
import static com.esb.internal.commons.ModuleProperties.*;
import static java.util.stream.Collectors.toSet;

abstract class AbstractDeserializer implements ModuleDeserializer {

    @Override
    public DeserializedModule deserialize() {

        Set<JSONObject> flows = objectsWithRoot(
                getResources(Flow.RESOURCE_DIRECTORY, FLOW.value()),
                Flow.ROOT_PROPERTY);

        Set<JSONObject> subflows = objectsWithRoot(
                getResources(Subflow.RESOURCE_DIRECTORY, SUBFLOW.value()),
                Subflow.ROOT_PROPERTY);

        Collection<JSONObject> configurations = getConfigurations();

        return new DeserializedModule(flows, subflows, configurations);
    }

    protected abstract List<URL> getResources(String directory, String suffix);


    private Set<JSONObject> objectsWithRoot(List<URL> resourcesURL, String rootPropertyName) {
        return resourcesURL.stream()
                .map(FileUtils::readFrom)
                .map(JsonParser::from)
                .filter(object -> object.has(rootPropertyName))
                .collect(toSet());
    }

    private Collection<JSONObject> getConfigurations() {
        List<URL> resourcesURL = getResources(Config.RESOURCE_DIRECTORY, FLOW_CONFIG.value());
        return resourcesURL.stream()
                .map(FileUtils::readFrom)
                .map(JsonParser::from)
                .collect(toSet());
    }
}
