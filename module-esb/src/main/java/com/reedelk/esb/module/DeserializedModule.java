package com.reedelk.esb.module;

import com.reedelk.esb.services.resource.ResourceLoader;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Set;

public class DeserializedModule {

    private final Set<JSONObject> flows;
    private final Set<JSONObject> subflows;
    private final Collection<JSONObject> configurations;

    private final Collection<ResourceLoader> scripts;
    private final Collection<ResourceLoader> metadata;

    public DeserializedModule(Set<JSONObject> flows,
                              Set<JSONObject> subflows,
                              Collection<JSONObject> configurations,
                              Collection<ResourceLoader> scripts,
                              Collection<ResourceLoader> metadata) {
        this.flows = flows;
        this.scripts = scripts;
        this.subflows = subflows;
        this.metadata = metadata;
        this.configurations = configurations;
    }

    public Set<JSONObject> getFlows() {
        return flows;
    }

    public Set<JSONObject> getSubflows() {
        return subflows;
    }

    public Collection<JSONObject> getConfigurations() {
        return configurations;
    }

    public Collection<ResourceLoader> getScriptResources() {
        return scripts;
    }

    public Collection<ResourceLoader> getMetadataResources() {
        return metadata;
    }
}
