package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;

public interface FlowExecutor {

    Publisher<MessageContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<MessageContext> parentFlux);

}

