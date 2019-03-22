package com.esb.flow.component.builder;

import com.esb.api.exception.ESBException;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.internal.commons.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

import static com.esb.commons.Preconditions.checkState;

class FlowReferenceComponentBuilder implements Builder {

    private final ExecutionGraph graph;
    private final FlowBuilderContext context;

    FlowReferenceComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String flowReference = JsonParser.FlowReference.getRef(componentDefinition);

        // TODO: This is part of the validation to be done on components.
        checkState(flowReference != null,
                "configRef property inside a FlowReference component cannot be null");

        Set<JSONObject> subflows = context.getDeserializedModule().getSubflows();

        JSONObject subflow = findSubflowByReference(subflows, flowReference);
        JSONArray subflowComponents = JsonParser.Subflow.getSubflow(subflow);

        ExecutionNode currentNode = parent;
        for (int i = 0; i < subflowComponents.length(); i++) {
            JSONObject currentComponent = subflowComponents.getJSONObject(i);

            currentNode = ExecutionNodeBuilder.get()
                    .componentDefinition(currentComponent)
                    .parent(currentNode)
                    .context(context)
                    .graph(graph)
                    .build();
        }

        return currentNode;
    }

    private JSONObject findSubflowByReference(Set<JSONObject> subflows, String referenceName) {
        return subflows.stream()
                .filter(subflow -> JsonParser.Subflow.id(subflow).equals(referenceName))
                .findFirst()
                .orElseThrow(() -> new ESBException("Could not find Subflow with referenceId=" + referenceName));
    }
}
