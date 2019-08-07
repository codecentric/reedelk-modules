package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StopFluxBuilder implements FluxBuilder {

    @Override
    public Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux) {
        return parentFlux;
    }

    @Override
    public Mono<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<MessageContext> parentFlux) {
        return parentFlux;
    }
}
