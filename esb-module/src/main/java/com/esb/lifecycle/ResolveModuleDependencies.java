package com.esb.lifecycle;

import com.esb.commons.DeserializedModule;
import com.esb.commons.JsonParser;
import com.esb.commons.JsonPropertyValueCollector;
import com.esb.component.ComponentRegistry;
import com.esb.flow.ModulesManager;
import com.esb.module.Module;
import org.json.JSONException;
import org.osgi.framework.Bundle;

import java.util.Collection;
import java.util.HashSet;

public class ResolveModuleDependencies extends AbstractStep<Void, Module> {

    private final ComponentRegistry componentRegistry;
    private final ModulesManager modulesManager;

    public ResolveModuleDependencies(ComponentRegistry componentRegistry, ModulesManager modulesManager) {
        this.componentRegistry = componentRegistry;
        this.modulesManager = modulesManager;
    }

    @Override
    public Module run(Void nothing) {
        final Bundle bundle = bundle();

        Module module = Module.builder()
                .moduleId(bundle.getBundleId())
                .name(bundle.getSymbolicName())
                .moduleFilePath(bundle.getLocation())
                .version(bundle.getVersion().toString())
                .build();

        DeserializedModule deserializedModule;
        try {
            deserializedModule = deserializedModule(bundle);
        } catch (JSONException error) {
            module.error(error);
            modulesManager.add(module);
            return module;
        }

        if (deserializedModule.getFlows().isEmpty()) {
            // This is a module with only components (no flows). The state is Installed.
            modulesManager.add(module);
            return module;
        }

        Collection<String> resolvedComponents = collectFlowAndSubFlowComponentsUsedByModule(deserializedModule);
        Collection<String> unresolvedComponents = componentRegistry.unregisteredComponentsOf(resolvedComponents);

        // We remove the unresolved components from the set of resolved ones.
        // Resolved must not contain unresolved components.
        resolvedComponents.removeAll(unresolvedComponents);

        module.unresolve(unresolvedComponents, resolvedComponents);

        if (unresolvedComponents.isEmpty()) {
            module.resolve(resolvedComponents);
        }

        modulesManager.add(module);
        return module;
    }


    private Collection<String> collectFlowAndSubFlowComponentsUsedByModule(DeserializedModule deserializedModule) {
        JsonPropertyValueCollector collector = new JsonPropertyValueCollector(JsonParser.Implementor.name());
        Collection<String> flowComponentNames = collector.collect(deserializedModule.getFlows());
        Collection<String> subFlowComponentNames = collector.collect(deserializedModule.getSubflows());
        Collection<String> allComponentsUsedByModule = new HashSet<>();

        allComponentsUsedByModule.addAll(flowComponentNames);
        allComponentsUsedByModule.addAll(subFlowComponentNames);
        return allComponentsUsedByModule;
    }

}
