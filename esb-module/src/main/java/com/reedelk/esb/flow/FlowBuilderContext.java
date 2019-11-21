package com.reedelk.esb.flow;


import com.reedelk.esb.commons.ConfigPropertyAwareTypeFactory;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.file.ModuleId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;

public class FlowBuilderContext {

    private final Bundle bundle;
    private final ModulesManager modulesManager;
    private final DeserializedModule deserializedModule;
    private final ConfigPropertyAwareTypeFactory typeFactory;

    public FlowBuilderContext(Bundle bundle, ModulesManager modulesManager, DeserializedModule deserializedModule, ConfigPropertyAwareTypeFactory typeFactory) {
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

    public ModuleId instantiateModuleId() {
        return bundle::getBundleId;
    }

    public DeserializedModule getDeSerializedModule() {
        return deserializedModule;
    }

    public Object convert(Class<?> clazz, JSONObject componentDefinition, String propertyName) {
        return typeFactory.create(clazz, componentDefinition, propertyName);
    }

    public Object convert(Class<?> genericType, JSONArray array, int index) {
        return typeFactory.create(genericType, array, index);
    }
}
