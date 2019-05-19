package com.esb.module.deserializer;

import com.esb.internal.commons.FileUtils;
import com.esb.internal.commons.JsonParser;
import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.esb.commons.FileExtension.FLOW;
import static com.esb.commons.FileExtension.FLOW_CONFIG;
import static com.esb.internal.commons.ModuleProperties.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

abstract class AbstractDeserializer implements ModuleDeserializer {

    @Override
    public DeserializedModule deserialize() {

        Set<JSONObject> flows = objectsWithRoot(
                getResources(Flow.RESOURCE_DIRECTORY, FLOW.value()),
                Flow.ROOT_PROPERTY);

        Set<JSONObject> subflows = objectsWithRoot(
                getResources(Subflow.RESOURCE_DIRECTORY, FLOW.value()),
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
        Iterator<JSONObject> it = objectsWithRoot(
                getResources(Config.RESOURCE_DIRECTORY, FLOW_CONFIG.value()),
                Config.ROOT_PROPERTY)
                .iterator();

        if (!it.hasNext()) {
            return Collections.emptyList();
        } else {
            JSONArray configs = it.next().getJSONArray(Config.ROOT_PROPERTY);
            return StreamSupport
                    .stream(configs.spliterator(), false)
                    .map(o -> (JSONObject) o)
                    .collect(toList());
        }
    }

}
