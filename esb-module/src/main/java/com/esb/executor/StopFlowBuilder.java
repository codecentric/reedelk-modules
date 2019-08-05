package com.esb.executor;

import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;

public class StopFlowBuilder implements FlowBuilder {
    @Override
    public Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {
        return parentFlux;
    }
}
