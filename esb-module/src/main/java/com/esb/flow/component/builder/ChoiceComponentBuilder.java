package com.esb.flow.component.builder;


import com.esb.commons.ExecutionGraph;
import com.esb.commons.JsonParser;
import com.esb.component.Choice;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import org.json.JSONArray;
import org.json.JSONObject;

class ChoiceComponentBuilder implements Builder {

    private final ExecutionGraph graph;
    private final FlowBuilderContext context;

    ChoiceComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = JsonParser.Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode choiceExecutionNode = context.instantiateComponent(componentName);

        graph.putEdge(parent, choiceExecutionNode);

        JSONArray when = JsonParser.Choice.getWhen(componentDefinition);

        for (int i = 0; i < when.length(); i++) {
            ExecutionNode currentNode = choiceExecutionNode;

            JSONObject component = when.getJSONObject(i);
            String condition = JsonParser.Choice.getCondition(component);
            JSONArray next = JsonParser.Choice.getNext(component);

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
                    Choice choiceComponent = (Choice) choiceExecutionNode.getComponent();
                    choiceComponent.addPathExpressionPair(condition, lastNode);
                }

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        ExecutionNode currentNode = choiceExecutionNode;

        JSONArray otherwise = JsonParser.Choice.getOtherwise(componentDefinition);

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
                Choice choiceComponent = (Choice) choiceExecutionNode.getComponent();
                choiceComponent.addDefaultPath(lastNode);
            }

            currentNode = lastNode;

        }

        graph.putEdge(currentNode, stopComponent);
        return stopComponent;
    }

}
