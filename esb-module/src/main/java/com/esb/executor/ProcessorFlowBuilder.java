package com.esb.executor;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class ProcessorFlowBuilder implements FlowBuilder {

    @Override
    public Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {
        Processor processor = (Processor) executionNode.getComponent();

        Flux<ReactiveMessageContext> newParent = parentFlux.flatMap(context -> asyncProcessor(processor, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "Processor must be followed by exactly one node");

        return ExecutionFlowBuilder.build(next, graph, newParent);
    }

    private Flux<ReactiveMessageContext> asyncProcessor(Processor processor, ReactiveMessageContext messageWrapper) {

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

        return Flux.create(messageWrapperFluxSink -> {
            try {
                Message outMessage = processor.apply(messageWrapper.getMessage());
                messageWrapper.replace(outMessage);
                messageWrapperFluxSink.next(messageWrapper);
            } catch (Exception e) {
                messageWrapper.onError(e);
                messageWrapperFluxSink.complete();
            }
        });
            /*
        } else {
            throw new IllegalStateException("Illegal");
        }*/
    }
}
