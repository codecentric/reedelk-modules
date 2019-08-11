package com.reedelk.esb.flow;


import com.reedelk.esb.flow.component.builder.ExecutionNodeBuilder;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.commons.JsonParser;
import com.reedelk.runtime.component.Stop;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.reedelk.esb.commons.Preconditions.checkState;

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
