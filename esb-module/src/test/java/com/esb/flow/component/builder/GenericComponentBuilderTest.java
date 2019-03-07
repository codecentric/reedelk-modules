package com.esb.flow.component.builder;

import com.esb.flow.ExecutionNode;
import com.esb.flow.ExecutionNode.ReferencePair;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.test.utils.ComponentsBuilder;
import com.esb.test.utils.TestComponent;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Generic Component Builder")
class GenericComponentBuilderTest {

    @Mock
    private ExecutionGraph graph;
    @Mock
    private ExecutionNode parent;
    @Mock
    private FlowBuilderContext context;

    private GenericComponentBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new GenericComponentBuilder(graph, context);
    }

    @Nested
    @DisplayName("Graph construction")
    class GraphConstruction {

        @Test
        void shouldCorrectlyPutGraphEdge() {
            // Given
            JSONObject componentDefinition = ComponentsBuilder.forComponent(TestComponent.class)
                    .build();

            ExecutionNode en = new ExecutionNode(new ReferencePair<>(new TestComponent()));
            mockInstantiation(en);

            // When
            builder.build(parent, componentDefinition);

            // Then
            verify(graph).putEdge(parent, en);
            verifyNoMoreInteractions(graph);
        }

    }

    private void mockInstantiation(ExecutionNode executionNode) {
        doReturn(executionNode)
                .when(context)
                .instantiateComponent(executionNode.getComponent().getClass().getName());
    }

}
