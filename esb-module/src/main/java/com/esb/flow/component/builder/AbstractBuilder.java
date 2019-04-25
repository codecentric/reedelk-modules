package com.esb.flow.component.builder;

import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;

public abstract class AbstractBuilder implements Builder {

    protected final ExecutionGraph graph;
    protected final FlowBuilderContext context;

    AbstractBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }

}
