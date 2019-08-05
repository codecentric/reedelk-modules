package com.esb.executor;

import com.esb.api.component.ResultCallback;
import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class FlowExecutor {

    private final ExecutionGraph graph;
    private SinkListener listener;

    public FlowExecutor(ExecutionGraph graph) {
        this.graph = graph;
        buildExecutionChain();
    }

    private void buildExecutionChain() {
        ConnectableFlux<ReactiveMessageContext> publisher =
                Flux.<ReactiveMessageContext>create(sink -> listener = sink::next)
                        .publish();

        ExecutionNode root = graph.getRoot();
        Collection<ExecutionNode> nextExecutorNodes = graph.successors(root);

        ExecutionNode nodeAfterRoot = checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                "Root must be followed by exactly one node");

        ExecutionFlowBuilder.build(nodeAfterRoot, graph, publisher)
                .map(messageWrapper -> {
                    messageWrapper.onDone();
                    return messageWrapper;
                }).subscribe();

        publisher.connect();
    }

    public void onEvent(Message message, ResultCallback resultCallback) {
        listener.onInput(new ReactiveMessageContext(message, resultCallback));
    }

    interface SinkListener {
        void onInput(ReactiveMessageContext context);
    }

}
