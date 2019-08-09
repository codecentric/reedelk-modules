package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;

public class StopFlowExecutor implements FlowExecutor {
    /**
     * When we find a stop node, we just return the parent flux.
     * Nothing needs to be added to the flux anymore.
     */
    @Override
    public Publisher<MessageContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<MessageContext> publisher) {
        return publisher;
    }
}
