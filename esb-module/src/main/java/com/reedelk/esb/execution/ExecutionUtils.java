package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import reactor.core.publisher.SynchronousSink;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.reedelk.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

class ExecutionUtils {
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
                "Expected only one following node");
    }

    static BiConsumer<MessageAndContext, SynchronousSink<MessageAndContext>> nullSafeMap(Function<MessageAndContext, MessageAndContext> mapper) {
        return (event, sink) -> {
            // TODO:If it is null this still needs to notify the end otherwise we wouldneverreturn from HTTP request.
            if (event != null) {
                MessageAndContext result = mapper.apply(event);
                sink.next(result);
            }
        };
    }
}
