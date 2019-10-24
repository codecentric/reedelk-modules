package com.reedelk.esb.lifecycle;

import com.reedelk.esb.commons.ConfigPropertyAwareJsonTypeConverter;
import com.reedelk.esb.commons.Log;
import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.flow.ErrorStateFlow;
import com.reedelk.esb.flow.Flow;
import com.reedelk.esb.flow.FlowBuilder;
import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.esb.module.state.ModuleState;
import com.reedelk.runtime.commons.JsonParser;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class BuildModule extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(BuildModule.class);

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

        String flowId = JsonParser.Flow.id(flowDefinition);
        String flowTitle = JsonParser.Flow.hasTitle(flowDefinition) ?
                JsonParser.Flow.title(flowDefinition) : null;

        ModulesManager modulesManager = modulesManager();
        ConfigPropertyAwareJsonTypeConverter converter = new ConfigPropertyAwareJsonTypeConverter(configurationService());

        try {
            FlowBuilderContext context = new FlowBuilderContext(bundle, modulesManager, deserializedModule, converter);
            FlowBuilder flowBuilder = new FlowBuilder(context);
            flowBuilder.build(flowGraph, flowDefinition);
            return new Flow(flowId, flowTitle, flowGraph, executionEngine);
        } catch (Exception exception) {
            Log.buildException(logger, flowDefinition, flowId, exception);
            return new ErrorStateFlow(flowId, flowTitle, flowGraph, executionEngine, exception);
        }
    }
}
