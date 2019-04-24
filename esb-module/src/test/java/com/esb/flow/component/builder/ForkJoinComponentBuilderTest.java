package com.esb.flow.component.builder;

import com.esb.component.ForkWrapper;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.ExecutionNode.ReferencePair;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.test.utils.ComponentsBuilder;
import com.esb.test.utils.TestComponent;
import com.esb.test.utils.TestJoinComponent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForkJoinComponentBuilderTest {

    private final String COMPONENT_1_NAME = TestComponent.class.getName() + "1";
    private final String COMPONENT_4_NAME = TestComponent.class.getName() + "4";
    private final String COMPONENT_5_NAME = TestComponent.class.getName() + "5";
    private final String COMPONENT_6_NAME = TestComponent.class.getName() + "6";
    private final Class JOIN_COMPONENT_NAME = TestJoinComponent.class;

    @Mock
    private ExecutionGraph graph;
    @Mock
    private FlowBuilderContext context;
    @Mock
    private ExecutionNode parentExecutionNode;
    @Mock
    private ExecutionNode testComponent1ExecutionNode;
    @Mock
    private ExecutionNode testComponent4ExecutionNode;
    @Mock
    private ExecutionNode testComponent5ExecutionNode;
    @Mock
    private ExecutionNode testComponent6ExecutionNode;
    @Mock
    private ExecutionNode joinComponentExecutionNode;


    private ExecutionNode stopExecutionNode = new ExecutionNode(new ReferencePair<>(new Stop()));
    private ExecutionNode forkJoinExecutionNode = new ExecutionNode(new ReferencePair<>(new ForkWrapper()));

    @BeforeEach
    void setUp() {
        doReturn(new TestComponent()).when(testComponent1ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent4ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent5ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent6ExecutionNode).getComponent();
        doReturn(new TestJoinComponent()).when(joinComponentExecutionNode).getComponent();

        doReturn(stopExecutionNode).when(context).instantiateComponent(Stop.class);
        doReturn(forkJoinExecutionNode).when(context).instantiateComponent(ForkWrapper.class.getName());
        doReturn(testComponent1ExecutionNode).when(context).instantiateComponent(COMPONENT_1_NAME);
        doReturn(testComponent4ExecutionNode).when(context).instantiateComponent(COMPONENT_4_NAME);
        doReturn(testComponent5ExecutionNode).when(context).instantiateComponent(COMPONENT_5_NAME);
        doReturn(testComponent6ExecutionNode).when(context).instantiateComponent(COMPONENT_6_NAME);
        doReturn(joinComponentExecutionNode).when(context).instantiateComponent(JOIN_COMPONENT_NAME.getName());
    }

    @Test
    void shouldCorrectlyHandleForkJoinComponent() {
        // Given
        JSONArray forkArray = new JSONArray();
        forkArray.put(createNextObject(COMPONENT_6_NAME, COMPONENT_5_NAME));
        forkArray.put(createNextObject(COMPONENT_1_NAME, COMPONENT_4_NAME));

        JSONObject componentDefinition = ComponentsBuilder.forComponent(ForkWrapper.class)
                .with("threadPoolSize", 3)
                .with("fork", forkArray)
                .with("join", ComponentsBuilder.forComponent(JOIN_COMPONENT_NAME)
                        .with("prop1", "Test")
                        .with("prop2", 3L)
                        .build())
                .build();

        ForkJoinComponentBuilder builder = new ForkJoinComponentBuilder(graph, context);

        // When
        ExecutionNode lastNode = builder.build(parentExecutionNode, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(joinComponentExecutionNode);

        verify(graph).putEdge(parentExecutionNode, forkJoinExecutionNode);

        // First Fork
        verify(graph).putEdge(forkJoinExecutionNode, testComponent6ExecutionNode);
        verify(graph).putEdge(testComponent6ExecutionNode, testComponent5ExecutionNode);
        verify(graph).putEdge(testComponent5ExecutionNode, stopExecutionNode);

        // Second Fork
        verify(graph).putEdge(forkJoinExecutionNode, testComponent1ExecutionNode);
        verify(graph).putEdge(testComponent1ExecutionNode, testComponent4ExecutionNode);
        verify(graph).putEdge(testComponent4ExecutionNode, stopExecutionNode);

        // Join
        verify(graph).putEdge(stopExecutionNode, joinComponentExecutionNode);
        verify(graph, never()).putEdge(parentExecutionNode, joinComponentExecutionNode);

    }

    private JSONObject createNextObject(String... componentNames) {
        JSONObject nextObject = new JSONObject();
        nextObject.put("next", ComponentsBuilder.createNextComponentsArray(componentNames));
        return nextObject;
    }
}
