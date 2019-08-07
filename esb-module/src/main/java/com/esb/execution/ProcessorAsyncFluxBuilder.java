package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.component.ProcessorAsync;
import com.esb.api.message.Message;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class ProcessorAsyncFluxBuilder implements FluxBuilder {


    @Override
    public Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux) {
        ProcessorAsync processorAsync = (ProcessorAsync) executionNode.getComponent();

        Flux<MessageContext> newParent = parentFlux.flatMap(context -> processorAsyncMono(processorAsync, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "ProcessorSync must be followed by exactly one node");

        return ExecutionFluxBuilder.get()
                .build(next, graph, newParent);
    }

    @Override
    public Mono<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Mono<MessageContext> parentFlux) {
        Scheduler scheduler = Schedulers.newElastic("Elastic-AsyncProcessor");

        ProcessorAsync processorSync = (ProcessorAsync) executionNode.getComponent();

        Mono<MessageContext> newParent = parentFlux.flatMap(context ->
                processorAsyncMono(processorSync, context).subscribeOn(scheduler));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "ProcessorSync must be followed by exactly one node");

        return ExecutionFluxBuilder.get()
                .build(next, graph, newParent);
    }


    private static Mono<MessageContext> processorAsyncMono(ProcessorAsync processorAsync, MessageContext messageWrapper) {
        return Mono.create(sink -> {
            try {
                processorAsync.apply(messageWrapper.getMessage(), new OnResult() {
                    @Override
                    public void onResult(Message message) {
                        messageWrapper.replace(message);
                        sink.success(messageWrapper);
                    }

                    @Override
                    public void onError(Throwable e) {
                        messageWrapper.onError(e);
                        // Complete without any value (meaning the flow stops)
                        sink.success();
                    }
                });
            } catch (Exception e) {
                messageWrapper.onError(e);
                // Complete without any value
                sink.success();
            }
        });
    }

}
