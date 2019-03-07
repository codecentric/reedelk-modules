package com.esb.flow.component.builder;

import com.esb.api.exception.ESBException;
import com.esb.commons.Graph;
import com.esb.commons.JsonParser;
import com.esb.component.Choice;
import com.esb.component.FlowReference;
import com.esb.component.Fork;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionNodeBuilder {

    private Graph graph;
    private ExecutionNode parent;
    private FlowBuilderContext context;
    private JSONObject componentDefinition;

    private static final Class<? extends Builder> GENERIC_HANDLER = GenericComponentBuilder.class;

    private static final Map<String, Class<? extends Builder>> COMPONENT_NAME_HANDLER;
    static {
        Map<String, Class<? extends Builder>> tmp = new HashMap<>();
        tmp.put(Fork.class.getName(), ForkJoinComponentBuilder.class);
        tmp.put(Choice.class.getName(), ChoiceComponentBuilder.class);
        tmp.put(FlowReference.class.getName(), FlowReferenceComponentBuilder.class);
        COMPONENT_NAME_HANDLER = Collections.unmodifiableMap(tmp);
    }

    private ExecutionNodeBuilder() {
    }

    public ExecutionNodeBuilder context(FlowBuilderContext context) {
        this.context = context;
        return this;
    }

    public ExecutionNodeBuilder graph(Graph graph) {
        this.graph = graph;
        return this;
    }

    public ExecutionNodeBuilder parent(ExecutionNode parent) {
        this.parent = parent;
        return this;
    }

    public ExecutionNodeBuilder componentDefinition(JSONObject componentDefinition) {
        this.componentDefinition = componentDefinition;
        return this;
    }

    public static ExecutionNodeBuilder get() {
        return new ExecutionNodeBuilder();
    }

    public ExecutionNode build() {
        String componentName = JsonParser.Implementor.name(componentDefinition);
        Class<? extends Builder> builderClazz = COMPONENT_NAME_HANDLER.getOrDefault(componentName, GENERIC_HANDLER);

        return instantiateBuilder(graph, context, builderClazz)
                .build(parent, componentDefinition);
    }

    private static Builder instantiateBuilder(Graph graph, FlowBuilderContext context, Class<? extends Builder> builderClazz) {
        try {
            return builderClazz
                    .getDeclaredConstructor(Graph.class, FlowBuilderContext.class)
                    .newInstance(graph, context);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
    }

}
