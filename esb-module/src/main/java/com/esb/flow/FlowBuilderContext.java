package com.esb.flow;


import com.esb.api.component.Implementor;
import com.esb.commons.DeserializedModule;
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
