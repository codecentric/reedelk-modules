package com.esb.lifecycle;

import com.esb.commons.JsonParser;
import com.esb.commons.JsonPropertyValueCollector;
import com.esb.component.ComponentRegistry;
import com.esb.module.DeserializedModule;
import com.esb.module.Module;

import java.util.Collection;
import java.util.HashSet;

public class ResolveModuleDependencies extends AbstractStep<Module, Module> {

    private final ComponentRegistry componentRegistry;

    public ResolveModuleDependencies(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @Override
    public Module run(Module module) {
        DeserializedModule deserializedModule;
        try {
            deserializedModule = module.deserialize();
        } catch (Exception error) {
            module.error(error);
            return module;
        }

        // This is a module with only components and there are no Flows.
        if (deserializedModule.getFlows().isEmpty()) {
            return module;
        }

        Collection<String> resolvedComponents = collectFlowAndSubFlowImplementorsValues(deserializedModule);
        Collection<String> unresolvedComponents = componentRegistry.unregisteredComponentsOf(resolvedComponents);

        // We remove the unresolved components from the set of resolved ones.
        // Resolved must not contain unresolved components.
        resolvedComponents.removeAll(unresolvedComponents);

        module.unresolve(unresolvedComponents, resolvedComponents);

        if (unresolvedComponents.isEmpty()) {
            module.resolve(resolvedComponents);
        }

        return module;
    }


    private Collection<String> collectFlowAndSubFlowImplementorsValues(DeserializedModule deserializedModule) {
        JsonPropertyValueCollector collector = new JsonPropertyValueCollector(JsonParser.Implementor.name());
        Collection<String> flowComponentNames = collector.collect(deserializedModule.getFlows());
        Collection<String> subFlowComponentNames = collector.collect(deserializedModule.getSubflows());
        Collection<String> allComponentsUsedByModule = new HashSet<>();

        allComponentsUsedByModule.addAll(flowComponentNames);
        allComponentsUsedByModule.addAll(subFlowComponentNames);
        return allComponentsUsedByModule;
    }

}
