package com.reedelk.esb.module;

import com.reedelk.esb.module.deserializer.ScriptResource;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Set;

public class DeserializedModule {

    private final Set<JSONObject> flows;
    private final Set<JSONObject> subflows;
    private final Collection<JSONObject> configurations;

    private final Collection<ScriptResource> scripts;

    public DeserializedModule(Set<JSONObject> flows,
                              Set<JSONObject> subflows,
                              Collection<JSONObject> configurations,
                              Collection<ScriptResource> scripts) {
        this.flows = flows;
        this.scripts = scripts;
        this.subflows = subflows;
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

    public Collection<ScriptResource> getScripts() {
        return scripts;
    }
}
