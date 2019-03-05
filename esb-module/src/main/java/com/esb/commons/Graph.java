package com.esb.commons;

import com.esb.api.component.Inbound;
import com.esb.flow.ExecutionNode;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Set;
import java.util.function.Consumer;

import static com.esb.commons.Preconditions.checkState;
import static com.google.common.graph.Traverser.forGraph;

public class Graph {

    private final MutableGraph<ExecutionNode> executionGraph;

    private ExecutionNode root;

    private Graph(final MutableGraph<ExecutionNode> executionGraph) {
        this.executionGraph = executionGraph;
    }

    public static Graph build() {
        MutableGraph<ExecutionNode> executionGraph = GraphBuilder.directed().build();
        return new Graph(executionGraph);
    }

    public void putEdge(ExecutionNode n1, ExecutionNode n2) {
        // If the parent is null, then the current execution node is the FIRST node of the graph
        if (n1 == null) {
            checkState(root == null, "Root must be null for first component");
            checkState(n2.getComponent() instanceof Inbound, "First component must be Inbound");
            root = n2;
            executionGraph.addNode(n2);
        } else {
            executionGraph.putEdge(n1, n2);
        }
    }

    public ExecutionNode getRoot() {
        return root;
    }

    public Set<ExecutionNode> successors(ExecutionNode executionNode) {
        return executionGraph.successors(executionNode);
    }

    public void applyOnNodes(Consumer<ExecutionNode> function) {
        if (hasRoot()) {
            forGraph(executionGraph)
                    .breadthFirst(root)
                    .forEach(function);
        }
    }

    public Optional<ExecutionNode> findOne(Predicate<ExecutionNode> predicate) {
        return Iterables.tryFind(forGraph(executionGraph)
                .breadthFirst(root), predicate);
    }

    private boolean hasRoot() {
        return root != null;
    }

}
