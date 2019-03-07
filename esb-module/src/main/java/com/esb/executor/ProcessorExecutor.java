package com.esb.executor;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.commons.ExecutionGraph;
import com.esb.flow.ExecutionNode;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class ProcessorExecutor implements Executor {

    @Override
    public ExecutionResult execute(ExecutionNode executionNode, Message message, ExecutionGraph graph) {
        Processor processor = (Processor) executionNode.getComponent();
        Message transformedMessage = processor.apply(message);

        Collection<ExecutionNode> followingExecutionNodes = graph.successors(executionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(
                followingExecutionNodes.stream(),
                "Processor must be followed by exactly one node");

        return Executors.execute(next, transformedMessage, graph);
    }

}
