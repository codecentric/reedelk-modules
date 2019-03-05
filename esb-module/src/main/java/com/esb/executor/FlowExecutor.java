package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.commons.Graph;
import com.esb.flow.ExecutionNode;

import java.util.Set;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class FlowExecutor {

    private final Graph graph;

    public FlowExecutor(Graph graph) {
        this.graph = graph;
    }

    public Message execute(Message message) {
        ExecutionNode root = graph.getRoot();

        Set<ExecutionNode> nextExecutorNodes = graph.successors(root);

        ExecutionNode nodeAfterRoot = checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                "Root must be followed by exactly one node");

        ExecutionResult result = Executors.execute(nodeAfterRoot, message, graph);

        return result.getMessage();
    }

}
