package com.reedelk.esb.flow.component.builder;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.commons.JsonParser;
import org.json.JSONObject;

public class GenericComponentBuilder extends AbstractBuilder {

    GenericComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = JsonParser.Implementor.name(componentDefinition);

        ExecutionNode executionNode = context.instantiateComponent(componentName);
        Component component = executionNode.getComponent();

        GenericComponentDeserializer deserializer = new GenericComponentDeserializer(executionNode, context);
        deserializer.deserialize(componentDefinition, component);

        graph.putEdge(parent, executionNode);
        return executionNode;
    }

}
