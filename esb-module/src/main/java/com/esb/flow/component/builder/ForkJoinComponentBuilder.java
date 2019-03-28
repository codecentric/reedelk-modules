package com.esb.flow.component.builder;

import com.esb.component.Fork;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.internal.commons.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

class ForkJoinComponentBuilder implements Builder {

    private final ExecutionGraph graph;
    private final FlowBuilderContext context;

    ForkJoinComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = JsonParser.Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode forkExecutionNode = context.instantiateComponent(componentName);

        Fork forkComponent = (Fork) forkExecutionNode.getComponent();

        int threadPoolSize = JsonParser.ForkJoin.getThreadPoolSize(componentDefinition);
        forkComponent.setThreadPoolSize(threadPoolSize);

        graph.putEdge(parent, forkExecutionNode);

        JSONArray fork = JsonParser.ForkJoin.getFork(componentDefinition);
        for (int i = 0; i < fork.length(); i++) {

            JSONObject nextObject = fork.getJSONObject(i);
            JSONArray nextComponents = JsonParser.ForkJoin.getNext(nextObject);

            ExecutionNode currentNode = forkExecutionNode;
            for (int j = 0; j < nextComponents.length(); j++) {

                JSONObject currentComponentDefinition = nextComponents.getJSONObject(j);
                ExecutionNode lastNode = ExecutionNodeBuilder.get()
                        .componentDefinition(currentComponentDefinition)
                        .parent(currentNode)
                        .context(context)
                        .graph(graph)
                        .build();

                // The first nextObject of A GIVEN fork path,
                // must be added as a fork execution node.
                if (j == 0) forkComponent.addForkNode(lastNode);

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        JSONObject joinComponent = JsonParser.ForkJoin.getJoin(componentDefinition);
        ExecutionNode joinExecutionNode = ExecutionNodeBuilder.get()
                .componentDefinition(joinComponent)
                .parent(stopComponent)
                .context(context)
                .graph(graph)
                .build();

        forkComponent.addJoin(joinExecutionNode);
        return joinExecutionNode;
    }

}
