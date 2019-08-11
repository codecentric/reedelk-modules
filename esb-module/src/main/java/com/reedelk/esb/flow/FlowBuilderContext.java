package com.reedelk.esb.flow;


import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.framework.Bundle;

public class FlowBuilderContext {

    private final Bundle bundle;
    private final DeserializedModule deserializedModule;
    private final ModulesManager modulesManager;

    public FlowBuilderContext(Bundle bundle, ModulesManager modulesManager, DeserializedModule deserializedModule) {
        this.bundle = bundle;
        this.modulesManager = modulesManager;
        this.deserializedModule = deserializedModule;
    }

    public ExecutionNode instantiateComponent(Class clazz) {
        return instantiateComponent(clazz.getName());
    }

    public ExecutionNode instantiateComponent(String componentName) {
        return modulesManager.instantiateComponent(bundle.getBundleContext(), componentName);
    }

    public Implementor instantiateImplementor(ExecutionNode executionNode, String implementorName) {
        return modulesManager.instantiateImplementor(bundle.getBundleContext(), executionNode, implementorName);
    }

    public DeserializedModule getDeserializedModule() {
        return deserializedModule;
    }

}
