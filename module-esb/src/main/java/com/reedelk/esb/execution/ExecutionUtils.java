package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;

import java.util.Collection;
import java.util.stream.Stream;

import static java.lang.String.format;

class ExecutionUtils {

    private ExecutionUtils() {
    }
    
    /**
     * Returns the successor node of the current node and it throws
     * an exception if a node was not found.
     *
     * @param current the current node for which we want to get the successor.
     * @param graph   the execution graph the current node belongs to.
     * @return the following execution node of the current node.
     * @throws IllegalStateException if  the next node is not present.
     */
    static ExecutionNode nextNode(ExecutionNode current, ExecutionGraph graph) {
        Collection<ExecutionNode> nextExecutorNodes = graph.successors(current);
        return checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                current.getComponent().getClass().getName(),
                nextExecutorNodes.size());
    }

    private static <T> T checkAtLeastOneAndGetOrThrow(Stream<T> stream, Object... args) {
        return stream.findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(format("Expected [%s] to have exactly one following node, but %d were found", args)));
    }
}
