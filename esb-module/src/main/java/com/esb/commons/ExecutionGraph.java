package com.esb.commons;

import com.esb.api.component.Inbound;
import com.esb.flow.ExecutionNode;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.esb.commons.Preconditions.checkState;

public class ExecutionGraph {

    private ExecutionGraphDirected executionGraph;
    private ExecutionNode root;

    private ExecutionGraph() {
        executionGraph = new ExecutionGraphDirected();
    }

    public static ExecutionGraph build() {
        return new ExecutionGraph();
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

    public Collection<ExecutionNode> successors(ExecutionNode executionNode) {
        return executionGraph.successors(executionNode);
    }

    public void applyOnNodes(Consumer<ExecutionNode> function) {
        if (hasRoot()) {
            executionGraph.breadthFirstTraversal(root, function);
        }
    }

    public Optional<ExecutionNode> findOne(Predicate<ExecutionNode> predicate) {
        return executionGraph.findOne(predicate);
    }

    private boolean hasRoot() {
        return root != null;
    }
}
