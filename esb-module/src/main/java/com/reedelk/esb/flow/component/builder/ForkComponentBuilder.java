package com.reedelk.esb.flow.component.builder;

import com.reedelk.esb.component.ForkWrapper;
import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.component.Stop;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.reedelk.runtime.commons.JsonParser.Fork;
import static com.reedelk.runtime.commons.JsonParser.Implementor;

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