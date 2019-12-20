package com.reedelk.esb.flow.deserializer;


import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule1;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.commons.TypeFactory;
import org.osgi.framework.Bundle;

public class FlowDeserializerContext {

    private final Bundle bundle;
    private final TypeFactory typeFactory;
    private final ModulesManager modulesManager;
    private final DeserializedModule1 deserializedModule1;

    public FlowDeserializerContext(Bundle bundle,
                                   ModulesManager modulesManager,
                                   DeserializedModule1 deserializedModule1,
                                   TypeFactory typeFactory) {
        this.bundle = bundle;
        this.typeFactory = typeFactory;
        this.modulesManager = modulesManager;
        this.deserializedModule1 = deserializedModule1;
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

    public DeserializedModule1 deserializedModule() {
        return deserializedModule1;
    }

    public TypeFactory typeFactory() {
        return typeFactory;
    }
}
