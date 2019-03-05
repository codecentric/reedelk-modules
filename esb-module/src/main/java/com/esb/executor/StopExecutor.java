package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.commons.Graph;
import com.esb.flow.ExecutionNode;

public class StopExecutor implements Executor {

    @Override
    public ExecutionResult execute(ExecutionNode executionNode, Message message, Graph graph) {
        return new ExecutionResult(message, executionNode);
    }

}
