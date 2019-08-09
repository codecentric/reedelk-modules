package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import com.esb.concurrency.SchedulerProvider;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class FlowExecutorEngine {

    private final ExecutionGraph graph;

    public FlowExecutorEngine(ExecutionGraph graph) {
        this.graph = graph;
    }

    /**
     * Executes the flow.
     */
    public void onEvent(Message message, OnResult onResult) {

        EventContext event = new EventContext(message, onResult);

        Publisher<EventContext> publisher =
                Mono.just(event)
                        .publishOn(SchedulerProvider.flow());

        ExecutionNode root = graph.getRoot();

        ExecutionNode nodeAfterRoot = ExecutionUtils.nextNodeOrThrow(root, graph);

        Publisher<EventContext> resultingPublisher =
                FlowExecutorFactory
                        .get()
                        .build(nodeAfterRoot, graph, publisher);

        Mono.from(resultingPublisher)
                .doOnError(onResult::onError)
                .subscribe(messageContext -> onResult.onResult(messageContext.getMessage()));
    }

}
