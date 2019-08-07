package com.esb.execution;

import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class FlowExecutor {

    private static final Scheduler ELASTIC = Schedulers.elastic();

    private SinkListener listener;

    public FlowExecutor(ExecutionGraph graph) {
        buildFlux(graph);
    }

    private void buildFlux(ExecutionGraph graph) {
        ConnectableFlux<MessageContext> publisher =
                Flux.<MessageContext>create(sink -> listener = sink::next)
                        .publishOn(ELASTIC)
                        .publish();

        ExecutionNode root = graph.getRoot();
        Collection<ExecutionNode> nextExecutorNodes = graph.successors(root);

        ExecutionNode nodeAfterRoot = checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                "Root must be followed by exactly one node");

        ExecutionFluxBuilder.get()
                .build(nodeAfterRoot, graph, publisher)
                .map(notifyFluxComplete)
                .subscribe();

        publisher.connect();
    }

    public void onEvent(Message message, OnResult onResult) {
        listener.onInput(new MessageContext(message, onResult));
    }

    interface SinkListener {
        void onInput(MessageContext context);
    }

    private final Function<MessageContext, MessageContext> notifyFluxComplete = messageContext -> {
        messageContext.onDone();
        return messageContext;
    };
}
