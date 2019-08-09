package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;

public interface FlowExecutor {

    Publisher<EventContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<EventContext> parentFlux);

}

