package com.esb.module.deserializer;

import com.esb.commons.FileUtils;
import com.esb.commons.JsonParser;
import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.esb.module.ModuleProperties.Config;
import static com.esb.module.ModuleProperties.Flow;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

abstract class AbstractDeserializer implements ModuleDeserializer {

    private static final String FLOW_ROOT_PROPERTY = "flow";
    private static final String SUBFLOW_ROOT_PROPERTY = "subflow";

    @Override
    public DeserializedModule deserialize() {
        Set<JSONObject> flows = objectsWithRoot(getResources(Flow.RESOURCE_DIRECTORY), FLOW_ROOT_PROPERTY);
        Set<JSONObject> subflows = objectsWithRoot(getResources(Flow.RESOURCE_DIRECTORY), SUBFLOW_ROOT_PROPERTY);
        Collection<JSONObject> configurations = getConfigurations();

        return new DeserializedModule(flows, subflows, configurations);
    }

    protected abstract List<URL> getResources(String directory);

    private Set<JSONObject> objectsWithRoot(List<URL> resourcesURL, String rootPropertyName) {
        return resourcesURL.stream()
                .map(FileUtils::readFrom)
                .map(JsonParser::from)
                .filter(object -> object.has(rootPropertyName))
                .collect(toSet());
    }

    private Collection<JSONObject> getConfigurations() {
        Iterator<JSONObject> it = objectsWithRoot(getResources(Config.RESOURCE_DIRECTORY), "configs").iterator();
        if (!it.hasNext()) {
            return Collections.emptyList();
        } else {
            JSONArray configs = it.next().getJSONArray("configs");
            return StreamSupport
                    .stream(configs.spliterator(), false)
                    .map(o -> (JSONObject) o)
                    .collect(toList());
        }
    }

}
