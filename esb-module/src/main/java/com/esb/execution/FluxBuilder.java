package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FluxBuilder {

    Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux);

    default Mono<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<MessageContext> parentFlux) {
        throw new UnsupportedOperationException();
    }
}
