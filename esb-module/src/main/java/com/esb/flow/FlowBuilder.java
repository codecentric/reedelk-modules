package com.esb.flow;


import com.esb.component.Stop;
import com.esb.flow.component.builder.ExecutionNodeBuilder;
import com.esb.graph.ExecutionGraph;
import com.esb.internal.commons.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.esb.commons.Preconditions.checkState;

public class FlowBuilder {

    private final FlowBuilderContext context;

    public FlowBuilder(FlowBuilderContext context) {
        this.context = context;
    }

    public void build(ExecutionGraph flowGraph, JSONObject flowStructure) {
        JSONArray flowComponents = JsonParser.Flow.flow(flowStructure);

        ExecutionNode current = null;
        for (Object componentDefinitionObject : flowComponents) {
            checkState(componentDefinitionObject instanceof JSONObject, "not a JSON Object");

            JSONObject componentDefinition = (JSONObject) componentDefinitionObject;

            current = ExecutionNodeBuilder.get()
                    .componentDefinition(componentDefinition)
                    .context(context)
                    .graph(flowGraph)
                    .parent(current)
                    .build();
        }

        // Last node of the graph is always a Stop node.
        ExecutionNode stopNode = context.instantiateComponent(Stop.class);
        flowGraph.putEdge(current, stopNode);
    }

}
