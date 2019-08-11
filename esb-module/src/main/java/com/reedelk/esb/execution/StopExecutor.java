package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;

public class StopExecutor implements FlowExecutor {
    /**
     * When we find a stop node, we just return the parent flux.
     * Nothing needs to be added to the flux anymore.
     */
    @Override
    public Publisher<EventContext> execute(Publisher<EventContext> publisher, ExecutionNode currentNode, ExecutionGraph graph) {
        return publisher;
    }
}
