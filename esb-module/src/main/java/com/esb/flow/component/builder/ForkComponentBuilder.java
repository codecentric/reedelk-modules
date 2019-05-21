package com.esb.flow.component.builder;

import com.esb.component.ForkWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.system.component.Stop;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.esb.internal.commons.JsonParser.Fork;
import static com.esb.internal.commons.JsonParser.Implementor;

class ForkComponentBuilder extends AbstractBuilder {

    ForkComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode forkExecutionNode = context.instantiateComponent(componentName);

        ForkWrapper forkWrapper = (ForkWrapper) forkExecutionNode.getComponent();

        int threadPoolSize = Fork.threadPoolSize(componentDefinition);
        forkWrapper.setThreadPoolSize(threadPoolSize);

        graph.putEdge(parent, forkExecutionNode);

        JSONArray fork = Fork.fork(componentDefinition);
        for (int i = 0; i < fork.length(); i++) {

            JSONObject nextObject = fork.getJSONObject(i);
            JSONArray nextComponents = Fork.next(nextObject);

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
                if (j == 0) forkWrapper.addForkNode(lastNode);

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        forkWrapper.setStopNode(stopComponent);

        return stopComponent;
    }

}
