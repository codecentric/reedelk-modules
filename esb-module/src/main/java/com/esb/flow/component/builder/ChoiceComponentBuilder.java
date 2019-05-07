package com.esb.flow.component.builder;


import com.esb.component.ChoiceWrapper;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.esb.internal.commons.JsonParser.Choice;
import static com.esb.internal.commons.JsonParser.Implementor;

class ChoiceComponentBuilder extends AbstractBuilder {

    ChoiceComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode choiceExecutionNode = context.instantiateComponent(componentName);
        ChoiceWrapper choiceWrapper = (ChoiceWrapper) choiceExecutionNode.getComponent();

        graph.putEdge(parent, choiceExecutionNode);

        JSONArray when = Choice.when(componentDefinition);

        for (int i = 0; i < when.length(); i++) {
            ExecutionNode currentNode = choiceExecutionNode;

            JSONObject component = when.getJSONObject(i);
            String condition = Choice.condition(component);
            JSONArray next = Choice.next(component);

            for (int j = 0; j < next.length(); j++) {
                JSONObject currentComponentDef = next.getJSONObject(j);
                ExecutionNode lastNode = ExecutionNodeBuilder.get()
                        .componentDefinition(currentComponentDef)
                        .parent(currentNode)
                        .context(context)
                        .graph(graph)
                        .build();

                // The first component of A GIVEN choice path,
                // must be added as a choice expression pair.
                if (j == 0) {
                    choiceWrapper.addPathExpressionPair(condition, lastNode);
                }

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        ExecutionNode currentNode = choiceExecutionNode;

        JSONArray otherwise = Choice.otherwise(componentDefinition);

        for (int j = 0; j < otherwise.length(); j++) {
            JSONObject currentComponentDef = otherwise.getJSONObject(j);

            ExecutionNode lastNode = ExecutionNodeBuilder.get()
                    .componentDefinition(currentComponentDef)
                    .parent(currentNode)
                    .context(context)
                    .graph(graph)
                    .build();

            // The first component of A GIVEN choice otherwise path,
            // must be added as default path.
            if (j == 0) {
                choiceWrapper.addDefaultPath(lastNode);
            }

            currentNode = lastNode;

        }

        graph.putEdge(currentNode, stopComponent);
        return stopComponent;
    }

}
