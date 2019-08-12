package com.reedelk.esb.execution;

import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static com.reedelk.esb.execution.ExecutionUtils.nextNode;

public class ProcessorAsyncExecutor implements FlowExecutor {

    @Override
    public Publisher<EventContext> execute(Publisher<EventContext> publisher, ExecutionNode currentNode, ExecutionGraph graph) {

        ProcessorAsync processorAsync = (ProcessorAsync) currentNode.getComponent();

        Mono<EventContext> parent = Mono.from(publisher)
                .flatMap(event -> sinkFromCallback(processorAsync, event)
                        .publishOn(SchedulerProvider.flow()));

        ExecutionNode next = nextNode(currentNode, graph);

        return FlowExecutorFactory.get().execute(parent, next, graph);
    }

    private static Mono<EventContext> sinkFromCallback(ProcessorAsync processor, EventContext messageWrapper) {
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
