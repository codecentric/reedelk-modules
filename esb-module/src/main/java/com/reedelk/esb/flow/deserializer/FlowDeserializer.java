package com.reedelk.esb.flow.deserializer;


import com.reedelk.esb.flow.deserializer.node.ExecutionNodeDeserializer;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.commons.JsonParser;
import com.reedelk.runtime.component.Stop;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.reedelk.esb.commons.Preconditions.checkState;

public class FlowDeserializer {

    private final FlowDeserializerContext context;

    public FlowDeserializer(FlowDeserializerContext context) {
        this.context = context;
    }

    public void deserialize(ExecutionGraph flowGraph, JSONObject flowStructure) {
        JSONArray flowComponents = JsonParser.Flow.flow(flowStructure);

        ExecutionNode current = null;
        for (Object componentDefinitionObject : flowComponents) {
            checkState(componentDefinitionObject instanceof JSONObject, "not a JSON Object");

            JSONObject componentDefinition = (JSONObject) componentDefinitionObject;

            current = ExecutionNodeDeserializer.get()
                    .componentDefinition(componentDefinition)
                    .context(context)
                    .graph(flowGraph)
                    .parent(current)
                    .deserialize();
        }

        // Last node of the graph is always a Stop node.
        ExecutionNode stopNode = context.instantiateComponent(Stop.class);
        flowGraph.putEdge(current, stopNode);
    }
}
