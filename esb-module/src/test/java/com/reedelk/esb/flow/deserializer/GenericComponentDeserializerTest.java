package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.graph.ExecutionNode.ReferencePair;
import com.reedelk.esb.test.utils.ComponentsBuilder;
import com.reedelk.esb.test.utils.TestComponent;
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
class GenericComponentDeserializerTest {

    @Mock
    private ExecutionGraph graph;
    @Mock
    private ExecutionNode parent;
    @Mock
    private FlowBuilderContext context;

    private GenericComponentDeserializer builder;

    @BeforeEach
    void setUp() {
        builder = new GenericComponentDeserializer(graph, context);
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
            builder.deserialize(parent, componentDefinition);

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
