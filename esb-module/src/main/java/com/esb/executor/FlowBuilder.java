package com.esb.executor;

import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlowBuilder {

    Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux);

    default Mono<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<ReactiveMessageContext> parentFlux) {
        throw new UnsupportedOperationException();
    }
}
