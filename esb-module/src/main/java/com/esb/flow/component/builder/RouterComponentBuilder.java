package com.esb.flow.component.builder;


import com.esb.component.RouterWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.system.component.Stop;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.esb.internal.commons.JsonParser.Implementor;
import static com.esb.internal.commons.JsonParser.Router;

class RouterComponentBuilder extends AbstractBuilder {

    RouterComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode routerExecutionNode = context.instantiateComponent(componentName);
        RouterWrapper routerWrapper = (RouterWrapper) routerExecutionNode.getComponent();

        graph.putEdge(parent, routerExecutionNode);

        JSONArray when = Router.when(componentDefinition);

        for (int i = 0; i < when.length(); i++) {
            ExecutionNode currentNode = routerExecutionNode;

            JSONObject component = when.getJSONObject(i);
            String condition = Router.condition(component);
            JSONArray next = Router.next(component);

            for (int j = 0; j < next.length(); j++) {
                JSONObject currentComponentDef = next.getJSONObject(j);
                ExecutionNode lastNode = ExecutionNodeBuilder.get()
                        .componentDefinition(currentComponentDef)
                        .parent(currentNode)
                        .context(context)
                        .graph(graph)
                        .build();

                // The first component of A GIVEN router path,
                // must be added as a router expression pair.
                if (j == 0) {
                    routerWrapper.addPathExpressionPair(condition, lastNode);
                }

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        return stopComponent;
    }

}
