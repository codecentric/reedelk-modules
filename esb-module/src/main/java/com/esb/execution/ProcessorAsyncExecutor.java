package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.component.ProcessorAsync;
import com.esb.api.message.Message;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ProcessorAsyncExecutor implements FlowExecutor {


    @Override
    public Publisher<EventContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<EventContext> publisher) {
        /**
        ProcessorAsync processorAsync = (ProcessorAsync) executionNode.getComponent();

         Flux<EventContext> newParent = publisher.flatMap(context -> processorAsyncMono(processorAsync, context));

        Collection<ExecutionNode> successors = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(successors.stream(),
                "ProcessorSync must be followed by exactly one node");

         return FlowExecutorFactory.get()
         .execute(next, graph, newParent);
         */
        return Mono.empty();
    }

    private static Mono<EventContext> processorAsyncMono(ProcessorAsync processorAsync, EventContext messageWrapper) {
        return Mono.create(sink -> {
            try {
                processorAsync.apply(messageWrapper.getMessage(), new OnResult() {
                    @Override
                    public void onResult(Message message) {
                        messageWrapper.replaceWith(message);
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
