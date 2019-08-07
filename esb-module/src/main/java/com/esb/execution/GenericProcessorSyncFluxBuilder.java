package com.esb.execution;

import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class GenericProcessorSyncFluxBuilder implements FluxBuilder {

    @Override
    public Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux) {
        ProcessorSync processorSync = (ProcessorSync) executionNode.getComponent();

        Flux<MessageContext> newParent = parentFlux.flatMap(context -> processorMono(processorSync, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "ProcessorSync must be followed by exactly one node");

        return ExecutionFluxBuilder.get()
                .build(next, graph, newParent);
    }

    @Override
    public Mono<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<MessageContext> parentFlux) {
        ProcessorSync processorSync = (ProcessorSync) executionNode.getComponent();

        Mono<MessageContext> newParent = parentFlux.flatMap(context -> processorMono(processorSync, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "ProcessorSync must be followed by exactly one node");

        return ExecutionFluxBuilder.get()
                .build(next, graph, newParent);
    }

    private static Mono<MessageContext> processorMono(ProcessorSync processorSync, MessageContext messageWrapper) {
        return Mono.create(sink -> {
            try {
                Message outMessage = processorSync.apply(messageWrapper.getMessage());
                messageWrapper.replace(outMessage);
                sink.success(messageWrapper);
            } catch (Exception e) {
                messageWrapper.onError(e);
                sink.success();
            }
        });
    }
}
