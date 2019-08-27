package com.reedelk.esb.flow.component.builder;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;

public abstract class AbstractBuilder implements Builder {

    protected final ExecutionGraph graph;
    protected final FlowBuilderContext context;

    AbstractBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }
}
