package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;

public abstract class AbstractDeserializer implements Deserializer {

    protected final ExecutionGraph graph;
    protected final FlowBuilderContext context;

    AbstractDeserializer(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }
}
