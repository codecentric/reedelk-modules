package com.esb.flow.component.builder;

import com.esb.commons.ESBExecutionGraph;
import com.esb.component.Choice;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.ExecutionNode.ReferencePair;
import com.esb.flow.FlowBuilderContext;
import com.esb.test.utils.ComponentsBuilder;
import com.esb.test.utils.TestComponent;
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
class ChoiceComponentBuilderTest {

    private static final String COMPONENT_1_NAME = TestComponent.class.getName() + "1";
    private static final String COMPONENT_2_NAME = TestComponent.class.getName() + "2";
    private static final String COMPONENT_3_NAME = TestComponent.class.getName() + "3";
    private static final String COMPONENT_4_NAME = TestComponent.class.getName() + "4";
    private static final String COMPONENT_5_NAME = TestComponent.class.getName() + "5";
    private static final String COMPONENT_6_NAME = TestComponent.class.getName() + "6";

    @Mock
    private ESBExecutionGraph graph;
    @Mock
    private FlowBuilderContext context;
    @Mock
    private ExecutionNode parentExecutionNode;
    @Mock
    private ExecutionNode testComponent1ExecutionNode;
    @Mock
    private ExecutionNode testComponent2ExecutionNode;
    @Mock
    private ExecutionNode testComponent3ExecutionNode;
    @Mock
    private ExecutionNode testComponent4ExecutionNode;
    @Mock
    private ExecutionNode testComponent5ExecutionNode;
    @Mock
    private ExecutionNode testComponent6ExecutionNode;

    private ExecutionNode stopExecutionNode = new ExecutionNode(new ReferencePair<>(new Stop()));
    private ExecutionNode choiceExecutionNode = new ExecutionNode(new ReferencePair<>(new Choice()));

    @BeforeEach
    void setUp() {
        doReturn(new TestComponent()).when(testComponent1ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent2ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent3ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent4ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent5ExecutionNode).getComponent();
        doReturn(new TestComponent()).when(testComponent6ExecutionNode).getComponent();

        doReturn(stopExecutionNode).when(context).instantiateComponent(Stop.class);
        doReturn(choiceExecutionNode).when(context).instantiateComponent(Choice.class.getName());
        doReturn(testComponent1ExecutionNode).when(context).instantiateComponent(COMPONENT_1_NAME);
        doReturn(testComponent2ExecutionNode).when(context).instantiateComponent(COMPONENT_2_NAME);
        doReturn(testComponent3ExecutionNode).when(context).instantiateComponent(COMPONENT_3_NAME);
        doReturn(testComponent4ExecutionNode).when(context).instantiateComponent(COMPONENT_4_NAME);
        doReturn(testComponent5ExecutionNode).when(context).instantiateComponent(COMPONENT_5_NAME);
        doReturn(testComponent6ExecutionNode).when(context).instantiateComponent(COMPONENT_6_NAME);
    }

    @Test
    void shouldCorrectlyHandleChoiceComponent() {
        // Given
        JSONArray whenArray = new JSONArray();
        whenArray.put(conditionalBranch("1 == 1", COMPONENT_3_NAME, COMPONENT_1_NAME));
        whenArray.put(conditionalBranch("'hello' == 'hello1'", COMPONENT_2_NAME, COMPONENT_4_NAME));

        JSONObject componentDefinition = ComponentsBuilder.forComponent(Choice.class)
                .with("when", whenArray)
                .with("otherwise", ComponentsBuilder.createNextComponentsArray(COMPONENT_6_NAME, COMPONENT_5_NAME))
                .build();

        ChoiceComponentBuilder builder = new ChoiceComponentBuilder(graph, context);

        // When
        ExecutionNode lastNode = builder.build(parentExecutionNode, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(stopExecutionNode);

        verify(graph).putEdge(parentExecutionNode, choiceExecutionNode);

        // First condition
        verify(graph).putEdge(choiceExecutionNode, testComponent3ExecutionNode);
        verify(graph).putEdge(testComponent3ExecutionNode, testComponent1ExecutionNode);
        verify(graph).putEdge(testComponent1ExecutionNode, stopExecutionNode);

        // Second condition
        verify(graph).putEdge(choiceExecutionNode, testComponent2ExecutionNode);
        verify(graph).putEdge(testComponent2ExecutionNode, testComponent4ExecutionNode);
        verify(graph).putEdge(testComponent4ExecutionNode, stopExecutionNode);

        // Otherwise
        verify(graph).putEdge(choiceExecutionNode, testComponent6ExecutionNode);
        verify(graph).putEdge(testComponent6ExecutionNode, testComponent5ExecutionNode);
        verify(graph).putEdge(testComponent5ExecutionNode, stopExecutionNode);

        verifyNoMoreInteractions(parentExecutionNode);
    }

    private JSONObject conditionalBranch(String condition, String... componentsNames) {
        JSONObject object = new JSONObject();
        object.put("condition", condition);
        object.put("next", ComponentsBuilder.createNextComponentsArray(componentsNames));
        return object;
    }


}
