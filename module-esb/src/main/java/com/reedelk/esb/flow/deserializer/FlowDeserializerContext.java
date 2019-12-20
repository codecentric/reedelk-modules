package com.reedelk.esb.flow.deserializer;


import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeSerializedModule;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.commons.TypeFactory;
import org.osgi.framework.Bundle;

public class FlowDeserializerContext {

    private final Bundle bundle;
    private final TypeFactory typeFactory;
    private final ModulesManager modulesManager;
    private final DeSerializedModule deSerializedModule;

    public FlowDeserializerContext(Bundle bundle,
                                   ModulesManager modulesManager,
                                   DeSerializedModule deSerializedModule,
                                   TypeFactory typeFactory) {
        this.bundle = bundle;
        this.typeFactory = typeFactory;
        this.modulesManager = modulesManager;
        this.deSerializedModule = deSerializedModule;
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

    public DeSerializedModule deserializedModule() {
        return deSerializedModule;
    }

    public TypeFactory typeFactory() {
        return typeFactory;
    }
}
