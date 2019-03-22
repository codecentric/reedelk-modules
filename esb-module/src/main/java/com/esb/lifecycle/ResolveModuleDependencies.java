package com.esb.lifecycle;

import com.esb.commons.JsonPropertyValueCollector;
import com.esb.internal.commons.JsonParser;
import com.esb.module.DeserializedModule;
import com.esb.module.Module;

import java.util.Collection;
import java.util.HashSet;

public class ResolveModuleDependencies extends AbstractStep<Module, Module> {

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

        Collection<String> resolvedComponents = collectImplementorDependencies(deserializedModule);
        Collection<String> unresolvedComponents = componentRegistry().unregisteredComponentsOf(resolvedComponents);

        // We remove the unresolved components from the set of resolved ones.
        // Resolved must not contain unresolved components.
        resolvedComponents.removeAll(unresolvedComponents);

        module.unresolve(unresolvedComponents, resolvedComponents);

        if (unresolvedComponents.isEmpty()) {
            module.resolve(resolvedComponents);
        }

        return module;
    }


    private Collection<String> collectImplementorDependencies(DeserializedModule deserializedModule) {
        JsonPropertyValueCollector collector = new JsonPropertyValueCollector(JsonParser.Implementor.name());
        Collection<String> flowImplementorNames = collector.collect(deserializedModule.getFlows());
        Collection<String> subFlowImplementorNames = collector.collect(deserializedModule.getSubflows());
        Collection<String> configImplementorNames = collector.collect(deserializedModule.getConfigurations());

        Collection<String> allComponentsUsedByModule = new HashSet<>();
        allComponentsUsedByModule.addAll(flowImplementorNames);
        allComponentsUsedByModule.addAll(subFlowImplementorNames);
        allComponentsUsedByModule.addAll(configImplementorNames);
        return allComponentsUsedByModule;
    }

}
