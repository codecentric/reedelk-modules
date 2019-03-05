package com.esb.flow;

import com.esb.commons.Graph;
import com.esb.commons.JsonParser;
import com.esb.component.Stop;
import com.esb.flow.builder.ExecutionNodeBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.esb.commons.Preconditions.checkState;

public class FlowBuilder {

    private final FlowBuilderContext context;

    public FlowBuilder(FlowBuilderContext context) {
        this.context = context;
    }

    public void build(Graph flowGraph, JSONObject flowStructure) {
        JSONArray flowComponents = JsonParser.Flow.getFlow(flowStructure);

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
