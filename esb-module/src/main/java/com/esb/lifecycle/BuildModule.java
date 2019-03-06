package com.esb.lifecycle;

import com.esb.api.exception.ESBException;
import com.esb.module.DeserializedModule;
import com.esb.commons.Graph;
import com.esb.commons.JsonParser;
import com.esb.commons.UniquePropertyValueValidator;
import com.esb.flow.*;
import com.esb.module.Module;
import com.esb.module.ModulesManager;
import com.esb.module.state.ModuleState;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class BuildModule extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(BuildModule.class);

    private final ModulesManager modulesManager;

    public BuildModule(ModulesManager modulesManager) {
        this.modulesManager = modulesManager;
    }

    @Override
    public Module run(Module module) {

        if (module.state() != ModuleState.RESOLVED) return module;

        Bundle bundle = bundle();

        DeserializedModule deserializedModule;
        try {
            deserializedModule = deserializedModule(bundle);
        } catch (Exception exception) {
            logger.error("Module deserialization", exception);

            module.error(exception);
            return module;
        }

        Set<Flow> flows = deserializedModule.getFlows().stream()
                .map(flowDefinition -> buildFlow(bundle, flowDefinition, deserializedModule))
                .collect(toSet());

        // If exists at least one flow in error state, then we release component references
        // for each flow built belonging to this Module.
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

        if (!UniquePropertyValueValidator.validate(flows, Flow::getFlowId)) {
            module.error(new ESBException("There are at least two flows with the same id. Flows Ids must be unique."));
            return module;
        }

        module.stop(flows);
        return module;
    }


    private Flow buildFlow(Bundle bundle, JSONObject flowDefinition, DeserializedModule deserializedModule) {
        Graph flowGraph = Graph.build();

        // TODO: THis should be part of the validation process of the flow with JSON schema.
        if (doesNotHaveValidFlowId(flowDefinition)) {
            return new ErrorStateFlow(flowGraph,
                    new ESBException("\"id\" property must be defined in the flow definition"));
        }

        String flowId = JsonParser.Flow.id(flowDefinition);

        FlowBuilderContext context = new FlowBuilderContext(bundle, modulesManager, deserializedModule);
        FlowBuilder flowBuilder = new FlowBuilder(context);

        try {
            flowBuilder.build(flowGraph, flowDefinition);
            return new Flow(flowId, flowGraph);
        } catch (Exception exception) {
            logger.error("BuildFlow", exception);
            return new ErrorStateFlow(flowId, flowGraph, exception);
        }
    }

    private void releaseComponentReferences(Bundle bundle, Collection<Flow> moduleFlows) {
        moduleFlows.forEach(flow -> flow.releaseReferences(bundle));
    }

    private boolean doesNotHaveValidFlowId(JSONObject flowDefinition) {
        return !JsonParser.Flow.hasId(flowDefinition) || isBlank(JsonParser.Flow.id(flowDefinition));
    }

}
