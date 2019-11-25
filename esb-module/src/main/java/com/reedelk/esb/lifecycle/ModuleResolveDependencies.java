package com.reedelk.esb.lifecycle;

import com.reedelk.esb.commons.JsonPropertyValueCollector;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.state.ModuleState;
import com.reedelk.runtime.commons.JsonParser;

import java.util.Collection;
import java.util.HashSet;

public class ModuleResolveDependencies extends AbstractStep<Module, Module> {

    @Override
    public Module run(Module module) {

        if (module.state() != ModuleState.INSTALLED) return module;

        deserialize(module).ifPresent(deSerializedModule -> {
            // If there are flows in the de-serialized module, then we resolve module
            // dependencies, otherwise it is a module without flows.
            if (!deSerializedModule.getFlows().isEmpty()) {

                Collection<String> resolvedComponents = collectImplementorDependencies(deSerializedModule);
                Collection<String> unresolvedComponents = componentRegistry().unregisteredComponentsOf(resolvedComponents);

                // We remove the unresolved components from the set of resolved ones.
                // Resolved must not contain unresolved components.
                resolvedComponents.removeAll(unresolvedComponents);

                module.unresolve(unresolvedComponents, resolvedComponents);

                if (unresolvedComponents.isEmpty()) {
                    module.resolve(resolvedComponents);
                }
            }
        });

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
