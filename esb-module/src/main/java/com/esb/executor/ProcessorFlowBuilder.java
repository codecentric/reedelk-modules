package com.esb.executor;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class ProcessorFlowBuilder implements FlowBuilder {

    @Override
    public Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {
        Processor processor = (Processor) executionNode.getComponent();

        Flux<ReactiveMessageContext> newParent = parentFlux.flatMap(context -> processorMono(processor, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "Processor must be followed by exactly one node");

        return ExecutionFlowBuilder.build(next, graph, newParent);
    }

    @Override
    public Mono<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<ReactiveMessageContext> parentFlux) {
        Processor processor = (Processor) executionNode.getComponent();

        Mono<ReactiveMessageContext> newParent = parentFlux.flatMap(context -> processorMono(processor, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "Processor must be followed by exactly one node");

        return ExecutionFlowBuilder.build(next, graph, newParent);
    }

    public static Mono<ReactiveMessageContext> processorMono(Processor processor, ReactiveMessageContext messageWrapper) {

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
