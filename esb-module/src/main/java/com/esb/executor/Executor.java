package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;

public interface Executor {

    ExecutionResult execute(ExecutionNode executionNode, Message message, ExecutionGraph graph);

}
