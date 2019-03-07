package com.esb.flow.component.builder;

import com.esb.api.component.Component;
import com.esb.commons.JsonParser;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import org.json.JSONObject;

public class GenericComponentBuilder implements Builder {

    private final ExecutionGraph graph;
    private final FlowBuilderContext context;

    GenericComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
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
