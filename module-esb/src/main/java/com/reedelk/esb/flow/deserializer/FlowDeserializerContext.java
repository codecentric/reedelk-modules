package com.reedelk.esb.flow.deserializer;


import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.commons.TypeFactory;
import org.osgi.framework.Bundle;

public class FlowDeserializerContext {

    private final Bundle bundle;
    private final TypeFactory typeFactory;
    private final ModulesManager modulesManager;
    private final DeserializedModule deserializedModule;

    public FlowDeserializerContext(Bundle bundle,
                                   ModulesManager modulesManager,
                                   DeserializedModule deserializedModule,
                                   TypeFactory typeFactory) {
        this.bundle = bundle;
        this.typeFactory = typeFactory;
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

    public DeserializedModule deserializedModule() {
        return deserializedModule;
    }

    public TypeFactory typeFactory() {
        return typeFactory;
    }
}
