package com.esb.module.deserializer;

import com.esb.commons.FileUtils;
import com.esb.commons.JsonParser;
import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.esb.commons.FileExtension.FLOW;
import static com.esb.commons.FileExtension.FLOW_CONFIG;
import static com.esb.module.ModuleProperties.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

abstract class AbstractDeserializer implements ModuleDeserializer {

    @Override
    public DeserializedModule deserialize() {

        Set<JSONObject> flows = objectsWithRoot(
                filteredResources(Flow.RESOURCE_DIRECTORY, FLOW.value()),
                Flow.ROOT_PROPERTY);

        Set<JSONObject> subflows = objectsWithRoot(
                filteredResources(Subflow.RESOURCE_DIRECTORY, FLOW.value()),
                Subflow.ROOT_PROPERTY);

        Collection<JSONObject> configurations = getConfigurations();

        return new DeserializedModule(flows, subflows, configurations);
    }

    protected abstract List<URL> getResources(String directory);

    private List<URL> filteredResources(String directory, String suffix) {
        return getResources(directory)
                .stream()
                .filter(url -> url.getFile().endsWith(suffix))
                .collect(Collectors.toList());
    }

    private Set<JSONObject> objectsWithRoot(List<URL> resourcesURL, String rootPropertyName) {
        return resourcesURL.stream()
                .map(FileUtils::readFrom)
                .map(JsonParser::from)
                .filter(object -> object.has(rootPropertyName))
                .collect(toSet());
    }

    private Collection<JSONObject> getConfigurations() {
        Iterator<JSONObject> it = objectsWithRoot(
                filteredResources(Config.RESOURCE_DIRECTORY, FLOW_CONFIG.value()),
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
