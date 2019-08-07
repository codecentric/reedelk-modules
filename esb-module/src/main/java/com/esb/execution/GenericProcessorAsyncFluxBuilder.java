package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.Flux;

public class GenericProcessorAsyncFluxBuilder implements FluxBuilder {
    @Override
    public Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux) {
        return null;
    }


    /**
     if (processorSync instanceof AsyncProcessor) {
     AsyncProcessor asyncProcessor = (AsyncProcessor) processorSync;
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

     } else if (processorSync instanceof SyncProcessor) {
     */

}
