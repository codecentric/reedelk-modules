package com.esb.flow.builder;

import com.esb.api.component.Component;
import com.esb.commons.Graph;
import com.esb.commons.JsonParser;
import com.esb.converter.JSONDeserializer;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import org.json.JSONObject;

public class GenericComponentBuilder implements Builder {

    private final Graph graph;
    private final FlowBuilderContext context;

    GenericComponentBuilder(Graph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = JsonParser.Implementor.name(componentDefinition);

        ExecutionNode executionNode = context.instantiateComponent(componentName);
        Component component = executionNode.getComponent();

        JSONDeserializer deserializer = new JSONDeserializer(executionNode, context);
        deserializer.deserialize(componentDefinition, component);

        graph.putEdge(parent, executionNode);
        return executionNode;
    }

}
