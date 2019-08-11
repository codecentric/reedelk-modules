package com.reedelk.esb.lifecycle;

import com.reedelk.esb.commons.UniquePropertyValueValidator;
import com.reedelk.esb.flow.ErrorStateFlow;
import com.reedelk.esb.flow.Flow;
import com.reedelk.esb.flow.FlowBuilder;
import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.esb.module.state.ModuleState;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.commons.JsonParser;
import com.reedelk.runtime.commons.StringUtils;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class BuildModule extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(BuildModule.class);

    @Override
    public Module run(Module module) {

        if (module.state() != ModuleState.RESOLVED) return module;

        Bundle bundle = bundle();

        DeserializedModule deserializedModule;
        try {
            deserializedModule = module.deserialize();
        } catch (Exception exception) {
            logger.error("Module deserialization", exception);

            module.error(exception);
            return module;
        }

        Set<Flow> flows = deserializedModule.getFlows().stream()
                .map(flowDefinition -> buildFlow(bundle, flowDefinition, deserializedModule))
                .collect(toSet());

        // If exists at least one flow in error state,
        // then we release component references for each flow built
        // belonging to this Module.
        Set<ErrorStateFlow> flowsWithErrors = flows.stream()
                .filter(flow -> flow instanceof ErrorStateFlow)
                .map(flow -> (ErrorStateFlow) flow)
                .collect(toSet());

        if (!flowsWithErrors.isEmpty()) {
            releaseComponentReferences(bundle, flows);

            module.error(flowsWithErrors.stream()
                    .map(ErrorStateFlow::getException)
                    .collect(toList()));

            return module;
        }

        // TODO: Validate somewhere else, like below, validation step or something.
        if (!UniquePropertyValueValidator.validate(flows, Flow::getFlowId)) {
            module.error(new ESBException("There are at least two flows with the same id. Flows Ids must be unique."));
            return module;
        }

        module.stop(flows);
        return module;
    }


    // TODO: Extract in its own class
    private Flow buildFlow(Bundle bundle, JSONObject flowDefinition, DeserializedModule deserializedModule) {
        ExecutionGraph flowGraph = ExecutionGraph.build();

        // TODO: This should be part of the validation process of the flow with JSON schema.
        if (invalidFlowId(flowDefinition)) {
            return new ErrorStateFlow(flowGraph,
                    new ESBException("\"id\" property must be defined in the flow definition"));
        }

        String flowId = JsonParser.Flow.id(flowDefinition);

        ModulesManager modulesManager = modulesManager();
        FlowBuilderContext context = new FlowBuilderContext(bundle, modulesManager, deserializedModule);
        FlowBuilder flowBuilder = new FlowBuilder(context);
        try {
            flowBuilder.build(flowGraph, flowDefinition);
            return new Flow(flowId, flowGraph);
        } catch (Exception exception) {
            String message = format("Error building flow with id [%s]", flowId);
            logger.error(message, exception);
            return new ErrorStateFlow(flowId, flowGraph, exception);
        }
    }

    private void releaseComponentReferences(Bundle bundle, Collection<Flow> moduleFlows) {
        moduleFlows.forEach(flow -> flow.releaseReferences(bundle));
    }

    private boolean invalidFlowId(JSONObject flowDefinition) {
        return !JsonParser.Flow.hasId(flowDefinition) ||
                StringUtils.isBlank(JsonParser.Flow.id(flowDefinition));
    }

}
