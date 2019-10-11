package com.reedelk.esb.execution;

import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static com.reedelk.esb.execution.ExecutionUtils.nextNode;

public class FlowExecutorEngine {

    private final ExecutionGraph graph;

    public FlowExecutorEngine(ExecutionGraph graph) {
        this.graph = graph;
    }

    /**
     * Executes the flow.
     */
    public void onEvent(Message message, OnResult onResult) {

        DefaultFlowContext defaultContext = new DefaultFlowContext();

        MessageAndContext event = new MessageAndContext(message, defaultContext);

        Publisher<MessageAndContext> publisher =
                Mono.just(event).publishOn(SchedulerProvider.flow());

        ExecutionNode root = graph.getRoot();

        ExecutionNode nodeAfterRoot = nextNode(root, graph);

        Publisher<MessageAndContext> resultingPublisher =
                FlowExecutorFactory.get().execute(publisher, nodeAfterRoot, graph);

        Mono.from(resultingPublisher)
                .doOnError(throwable -> onResult.onError(throwable, defaultContext))
                .subscribe(messageContext -> onResult.onResult(messageContext.getMessage(), defaultContext));
    }

}
