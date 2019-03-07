package com.esb.graph;

import com.esb.flow.ExecutionNode;
import com.esb.test.utils.TestComponent;
import com.esb.test.utils.TestInboundComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionGraphTest {

    private ExecutionGraph graph;

    @BeforeEach
    void setUp() {
        graph = ExecutionGraph.build();
    }

    @Test
    void shouldDoSomething() {
        // Given
        ExecutionNode node1 = new ExecutionNode(new ExecutionNode.ReferencePair<>(new TestInboundComponent()));
        ExecutionNode node2 = new ExecutionNode(new ExecutionNode.ReferencePair<>(new TestComponent()));

        // When
        graph.putEdge(null, node1);
        graph.putEdge(node1, node2);

        // Then
        Collection<ExecutionNode> successors = graph.successors(node1);
        assertThat(successors).hasSize(1);
        assertThat(successors).containsExactly(node2);
    }
}