package com.reedelk.esb.flow.deserializer.node;

import com.reedelk.esb.flow.deserializer.FlowDeserializerContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.commons.JsonParser;
import org.json.JSONObject;

public class GenericComponentDeserializer extends AbstractDeserializer {

    GenericComponentDeserializer(ExecutionGraph graph, FlowDeserializerContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode deserialize(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = JsonParser.Implementor.name(componentDefinition);

        ExecutionNode executionNode = context.instantiateComponent(componentName);
        Component component = executionNode.getComponent();

        ComponentDefinitionDeserializer deserializer = new ComponentDefinitionDeserializer(executionNode, context);
        deserializer.deserialize(componentDefinition, component);

        graph.putEdge(parent, executionNode);
        return executionNode;
    }
}
