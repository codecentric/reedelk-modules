package com.esb.module.deserializer;

import com.esb.commons.FileUtils;
import com.esb.commons.JsonParser;
import com.esb.module.DeserializedModule;
import com.esb.module.ModuleDeserializer;
import com.esb.module.ModuleProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.list;
import static java.util.stream.Collectors.toSet;

public class BundleDeserializer implements ModuleDeserializer {

    private final Bundle bundle;

    public BundleDeserializer(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public DeserializedModule deserialize() {
        Set<JSONObject> flows = getObjectsHavingRoot(
                bundle,
                getResources(bundle, ModuleProperties.Flow.RESOURCE_DIRECTORY),
                "flow");

        Set<JSONObject> subflows = getObjectsHavingRoot(
                bundle,
                getResources(bundle, ModuleProperties.Flow.RESOURCE_DIRECTORY),
                "subflow");

        Collection<JSONObject> configurations = getConfigurations(bundle);

        return new DeserializedModule(flows, subflows, configurations);
    }

    private Set<JSONObject> getObjectsHavingRoot(Bundle bundle, List<String> jsonResourcesToFilter, String rootPropertyName) {
        return jsonResourcesToFilter
                .stream()
                .map(bundle::getResource)
                .map(FileUtils::readFrom)
                .map(JsonParser::from)
                .filter(object -> object.has(rootPropertyName))
                .collect(toSet());
    }

    private List<String> getResources(Bundle bundle, String path) {
        Enumeration<String> entryPaths = bundle.getEntryPaths(path);
        return entryPaths != null ? list(entryPaths) : Collections.emptyList();
    }

    private Collection<JSONObject> getConfigurations(Bundle bundle) {
        Iterator<JSONObject> it = getObjectsHavingRoot(
                bundle,
                getResources(bundle, ModuleProperties.Config.RESOURCE_DIRECTORY),
                "configs")
                .iterator();

        if (!it.hasNext()) return Collections.emptyList();

        JSONArray configs = it.next().getJSONArray("configs");
        return StreamSupport
                .stream(configs.spliterator(), false)
                .map(o -> (JSONObject) o)
                .collect(Collectors.toList());
    }
}
