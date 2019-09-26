package com.reedelk.esb.flow.component.builder;


import com.reedelk.esb.component.RouterWrapper;
import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.script.DynamicBoolean;
import com.reedelk.runtime.component.Stop;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.reedelk.runtime.commons.JsonParser.Implementor;
import static com.reedelk.runtime.commons.JsonParser.Router;

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
                    DynamicBoolean expression = DynamicBoolean.from(condition);
                    routerWrapper.addExpressionAndPathPair(expression, lastNode);
                }

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        // We add the stop execution node related to this router,
        // so that we can use it when building the flux definition
        // for this Router node. Otherwise we would have to find
        // the stop component by navigating the graph and finding the
        // the common stop node across all the nodes in the Router scope.
        routerWrapper.setEndOfRouterStopNode(stopComponent);

        return stopComponent;
    }

}
