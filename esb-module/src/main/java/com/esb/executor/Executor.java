package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.commons.ExecutionGraph;
import com.esb.flow.ExecutionNode;

public interface Executor {

    ExecutionResult execute(ExecutionNode executionNode, Message message, ExecutionGraph graph);

}
