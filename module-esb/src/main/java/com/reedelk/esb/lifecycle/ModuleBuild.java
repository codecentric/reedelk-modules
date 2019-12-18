package com.reedelk.esb.lifecycle;

import com.reedelk.esb.commons.Log;
import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.flow.ErrorStateFlow;
import com.reedelk.esb.flow.Flow;
import com.reedelk.esb.flow.deserializer.FlowDeserializer;
import com.reedelk.esb.flow.deserializer.FlowDeserializerContext;
import com.reedelk.esb.flow.deserializer.typefactory.ConfigPropertyAwareTypeFactoryDecorator;
import com.reedelk.esb.flow.deserializer.typefactory.ScriptFunctionBodyResolverDecorator;
import com.reedelk.esb.flow.deserializer.typefactory.TypeFactoryContextAwareDecorator;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.esb.module.state.ModuleState;
import com.reedelk.runtime.api.commons.ModuleId;
import com.reedelk.runtime.commons.TypeFactory;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.reedelk.runtime.commons.JsonParser.Flow.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ModuleBuild extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(ModuleBuild.class);

    @Override
    public Module run(Module module) {

        if (module.state() != ModuleState.RESOLVED) return module;

        deserialize(module).ifPresent(deSerializedModule -> {

            Bundle bundle = bundle();

            Set<Flow> flows = deSerializedModule.getFlows().stream()
                    .map(flowDefinition -> buildFlow(bundle, flowDefinition, deSerializedModule))
                    .collect(toSet());

            // If exists at least one flow in error state,
            // then we release component references for each flow built
            // belonging to this Module.
            Set<ErrorStateFlow> flowsWithErrors = flows.stream()
                    .filter(flow -> flow instanceof ErrorStateFlow)
                    .map(flow -> (ErrorStateFlow) flow)
                    .collect(toSet());

            if (flowsWithErrors.isEmpty()) {
                module.stop(flows);
            } else {
                // If there are errors, we MUST release references
                // for the flows we have already built.
                flows.forEach(flow -> flow.releaseReferences(bundle));

                module.error(flowsWithErrors.stream()
                        .map(ErrorStateFlow::getException)
                        .collect(toList()));
            }
        });

        return module;
    }

    private Flow buildFlow(Bundle bundle, JSONObject flowDefinition, DeserializedModule deserializedModule) {
        ExecutionGraph flowGraph = ExecutionGraph.build();
        FlowExecutorEngine executionEngine = new FlowExecutorEngine(flowGraph);

        ModulesManager modulesManager = modulesManager();
        Module module = modulesManager.getModuleById(bundle.getBundleId());
        ModuleId moduleId = new ModuleId(module.id());

        TypeFactory typeFactory = TypeFactory.getInstance();
        typeFactory = new ScriptFunctionBodyResolverDecorator(typeFactory, deserializedModule);
        typeFactory = new ConfigPropertyAwareTypeFactoryDecorator(configurationService(), typeFactory);
        typeFactory = new TypeFactoryContextAwareDecorator(typeFactory, moduleId);

        String flowId = id(flowDefinition);
        String flowTitle = hasTitle(flowDefinition) ? title(flowDefinition) : null;

        try {
            FlowDeserializerContext context = new FlowDeserializerContext(bundle, modulesManager, deserializedModule, typeFactory);
            FlowDeserializer flowDeserializer = new FlowDeserializer(context);
            flowDeserializer.deserialize(flowGraph, flowDefinition);
            return new Flow(module.id(), module.name(), flowId, flowTitle, flowGraph, executionEngine);

        } catch (Exception exception) {
            Log.buildException(logger, flowDefinition, flowId, exception);
            return new ErrorStateFlow(module.id(), module.name(), flowId, flowTitle, flowGraph, executionEngine, exception);
        }
    }
}
