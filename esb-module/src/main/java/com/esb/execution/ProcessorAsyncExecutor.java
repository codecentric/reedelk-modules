package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.component.ProcessorAsync;
import com.esb.api.message.Message;
import com.esb.concurrency.SchedulerProvider;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static com.esb.execution.ExecutionUtils.nextNode;

public class ProcessorAsyncExecutor implements FlowExecutor {

    @Override
    public Publisher<EventContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<EventContext> publisher) {

        ProcessorAsync processorAsync = (ProcessorAsync) executionNode.getComponent();

        Mono<EventContext> parent = Mono.from(publisher)
                .flatMap(event -> mapProcessorAsync(processorAsync, event)
                        .publishOn(SchedulerProvider.flow()));

        ExecutionNode next = nextNode(executionNode, graph);

        return FlowExecutorFactory.get().build(next, graph, parent);
    }

    private static Mono<EventContext> mapProcessorAsync(ProcessorAsync processor, EventContext messageWrapper) {
        return Mono.create(sink -> {
            try {
                processor.apply(messageWrapper.getMessage(), new OnResult() {

                    @Override
                    public void onResult(Message message) {

                        messageWrapper.replaceWith(message);

                        sink.success(messageWrapper);
                    }

                    @Override
                    public void onError(Throwable e) {

                        sink.error(e);

                    }
                });
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

}
