package com.esb.executor;

import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;

public interface FlowBuilder {
    Flux<ReactiveMessageContext> build(ExecutionNode executionNode,
                                       ExecutionGraph graph,
                                       Flux<ReactiveMessageContext> parentFlux);
}
