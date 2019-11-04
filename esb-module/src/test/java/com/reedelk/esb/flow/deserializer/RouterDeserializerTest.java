package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.component.RouterWrapper;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.graph.ExecutionNode.ReferencePair;
import com.reedelk.esb.test.utils.ComponentsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.reedelk.runtime.component.Router.DEFAULT_CONDITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RouterDeserializerTest extends AbstractDeserializerTest {

    private ExecutionNode routerExecutionNode = new ExecutionNode(disposer, new ReferencePair<>(new RouterWrapper()));

    private RouterDeserializer deserializer;

    @BeforeEach
    public void setUp() {
        super.setUp();
        doReturn(routerExecutionNode).when(context).instantiateComponent(RouterWrapper.class.getName());
        deserializer = new RouterDeserializer(graph, context);
    }

    @Test
    void shouldCorrectlyHandleRouterComponent() {
        // Given
        JSONArray whenArray = new JSONArray();
        whenArray.put(conditionalBranch("#[1 == 1]", component3Name, component1Name));
        whenArray.put(conditionalBranch("#['hello' == 'hello1']", component2Name, component4Name));
        whenArray.put(conditionalBranch(DEFAULT_CONDITION.value(), component6Name, component5Name));

        JSONObject componentDefinition = ComponentsBuilder.forComponent(RouterWrapper.class)
                .with("when", whenArray)
                .build();

        // When
        ExecutionNode lastNode = deserializer.deserialize(parent, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(stopExecutionNode);

        verify(graph).putEdge(parent, routerExecutionNode);

        // First condition
        verify(graph).putEdge(routerExecutionNode, component3);
        verify(graph).putEdge(component3, component1);
        verify(graph).putEdge(component1, stopExecutionNode);

        // Second condition
        verify(graph).putEdge(routerExecutionNode, component2);
        verify(graph).putEdge(component2, component4);
        verify(graph).putEdge(component4, stopExecutionNode);

        // Otherwise
        verify(graph).putEdge(routerExecutionNode, component6);
        verify(graph).putEdge(component6, component5);
        verify(graph).putEdge(component5, stopExecutionNode);

        verifyNoMoreInteractions(parent);
    }

    private JSONObject conditionalBranch(String condition, String... componentsNames) {
        JSONObject object = new JSONObject();
        object.put("condition", condition);
        object.put("next", ComponentsBuilder.createNextComponentsArray(componentsNames));
        return object;
    }
}
