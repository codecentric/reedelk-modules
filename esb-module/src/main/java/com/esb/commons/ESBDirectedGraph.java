package com.esb.commons;

import com.esb.flow.ExecutionNode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.esb.commons.Preconditions.checkState;

public class ESBDirectedGraph {

    private Map<ExecutionNode, List<ExecutionNode>> adjacentNodes = new HashMap<>();

    public void addNode(ExecutionNode n2) {
        adjacentNodes.putIfAbsent(n2, new ArrayList<>());
    }

    public void putEdge(ExecutionNode n1, ExecutionNode n2) {
        checkState(adjacentNodes.containsKey(n1),
                "n1 must be already in graph in order to add an edge");
        if (!adjacentNodes.containsKey(n2)) {
            adjacentNodes.put(n2, new ArrayList<>());
        }
        adjacentNodes.get(n1).add(n2);
    }

    public Collection<ExecutionNode> successors(ExecutionNode executionNode) {
        return adjacentNodes.get(executionNode);
    }

    public void breadthFirstTraversal(ExecutionNode root, Consumer<ExecutionNode> visitor) {
        Set<ExecutionNode> visited = new LinkedHashSet<>();
        Queue<ExecutionNode> queue = new LinkedList<>();
        queue.add(root);
        visited.add(root);
        while (!queue.isEmpty()) {
            ExecutionNode executionNode = queue.poll();
            for (ExecutionNode en : adjacentNodes.get(executionNode)) {
                if (!visited.contains(en)) {
                    visited.add(en);
                    queue.add(en);
                }
                visitor.accept(en);
            }
        }
    }

    public Optional<ExecutionNode> findOne(Predicate<ExecutionNode> predicate) {
        return adjacentNodes.keySet()
                .stream()
                .filter(predicate)
                .findFirst();
    }
}
