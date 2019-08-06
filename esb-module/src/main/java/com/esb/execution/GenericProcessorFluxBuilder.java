package com.esb.execution;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class GenericProcessorFluxBuilder implements FluxBuilder {

    @Override
    public Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux) {
        Processor processor = (Processor) executionNode.getComponent();

        Flux<MessageContext> newParent = parentFlux.flatMap(context -> processorMono(processor, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "Processor must be followed by exactly one node");

        return ExecutionFluxBuilder.build(next, graph, newParent);
    }

    @Override
    public Mono<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<MessageContext> parentFlux) {
        Processor processor = (Processor) executionNode.getComponent();

        Mono<MessageContext> newParent = parentFlux.flatMap(context -> processorMono(processor, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "Processor must be followed by exactly one node");

        return ExecutionFluxBuilder.build(next, graph, newParent);
    }

    public static Mono<MessageContext> processorMono(Processor processor, MessageContext messageWrapper) {

        /**
         if (processor instanceof AsyncProcessor) {
         AsyncProcessor asyncProcessor = (AsyncProcessor) processor;
         return Flux.create(fluxSink -> {
         try {
         asyncProcessor.process(messageWrapper.message, wrapper -> {
         messageWrapper.message = wrapper;
         fluxSink.next(messageWrapper);
         });
         } catch (Exception e) {
         messageWrapper.onError(e);
         fluxSink.complete();
         }
         });

         } else if (processor instanceof SyncProcessor) {
         */

        return Mono.create(sink -> {
            try {
                Message outMessage = processor.apply(messageWrapper.getMessage());
                messageWrapper.replace(outMessage);
                sink.success(messageWrapper);
            } catch (Exception e) {
                messageWrapper.onError(e);
                sink.success();
            }
        });
            /*
        } else {
            throw new IllegalStateException("Illegal");
        }*/
    }
}
