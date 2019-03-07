package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;

public class StopExecutor implements Executor {

    @Override
    public ExecutionResult execute(ExecutionNode executionNode, Message message, ExecutionGraph graph) {
        return new ExecutionResult(message, executionNode);
    }

}
