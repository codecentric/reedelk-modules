package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;

import java.util.List;

class AbstractTestGraphBuilder {

    void buildSequence(ExecutionGraph graph, ExecutionNode start, ExecutionNode end, List<ExecutionNode> sequence) {
        ExecutionNode previous = start;
        for (ExecutionNode node : sequence) {
            graph.putEdge(previous, node);
            previous = node;
        }
        graph.putEdge(previous, end);
    }
}
