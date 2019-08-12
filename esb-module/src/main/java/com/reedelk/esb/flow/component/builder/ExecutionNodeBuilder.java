package com.reedelk.esb.flow.component.builder;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.commons.JsonParser;
import com.reedelk.runtime.component.FlowReference;
import com.reedelk.runtime.component.Fork;
import com.reedelk.runtime.component.Router;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionNodeBuilder {

    private ExecutionGraph graph;
    private ExecutionNode parent;
    private FlowBuilderContext context;
    private JSONObject componentDefinition;

    private static final Class<? extends Builder> GENERIC_HANDLER = GenericComponentBuilder.class;

    private static final Map<String, Class<? extends Builder>> COMPONENT_NAME_HANDLER;
    static {
        Map<String, Class<? extends Builder>> tmp = new HashMap<>();
        tmp.put(Fork.class.getName(), ForkComponentBuilder.class);
        tmp.put(Router.class.getName(), RouterComponentBuilder.class);
        tmp.put(FlowReference.class.getName(), FlowReferenceComponentBuilder.class);
        COMPONENT_NAME_HANDLER = Collections.unmodifiableMap(tmp);
    }

    private ExecutionNodeBuilder() {
    }

    public ExecutionNodeBuilder context(FlowBuilderContext context) {
        this.context = context;
        return this;
    }

    private static Builder instantiateBuilder(ExecutionGraph graph, FlowBuilderContext context, Class<? extends Builder> builderClazz) {
        try {
            return builderClazz
                    .getDeclaredConstructor(ExecutionGraph.class, FlowBuilderContext.class)
                    .newInstance(graph, context);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
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

        return instantiateBuilder(graph, context, builderClazz).build(parent, componentDefinition);
    }

    public ExecutionNodeBuilder graph(ExecutionGraph graph) {
        this.graph = graph;
        return this;
    }

}