package com.esb.commons;

import org.json.JSONObject;

import java.util.Collection;
import java.util.Set;

public class DeserializedModule {

    private final Set<JSONObject> flows;
    private final Set<JSONObject> subflows;
    private final Collection<JSONObject> configurations;

    public DeserializedModule(Set<JSONObject> flows, Set<JSONObject> subflows, Collection<JSONObject> configurations) {
        this.flows = flows;
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
}
