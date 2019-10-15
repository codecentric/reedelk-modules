package com.reedelk.esb.lifecycle;

import com.reedelk.esb.commons.JsonPropertyValueCollector;
import com.reedelk.esb.commons.Log;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.runtime.commons.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

public class ResolveModuleDependencies extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(ResolveModuleDependencies.class);

    @Override
    public Module run(Module module) {
        DeserializedModule deserializedModule;
        try {
            deserializedModule = module.deserialize();
        } catch (Exception exception) {
            Log.deserializationException(logger, module, exception);
            module.error(exception);
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
