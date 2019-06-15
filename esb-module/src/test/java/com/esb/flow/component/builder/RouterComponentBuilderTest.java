package com.esb.flow.component.builder;

import com.esb.component.RouterWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.flow.ExecutionNode.ReferencePair;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import com.esb.system.component.Stop;
import com.esb.test.utils.ComponentsBuilder;
import com.esb.test.utils.TestComponent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.esb.system.component.Router.DEFAULT_CONDITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouterComponentBuilderTest {

    private final String COMPONENT_1_NAME = TestComponent.class.getName() + "1";
    private final String COMPONENT_2_NAME = TestComponent.class.getName() + "2";
    private final String COMPONENT_3_NAME = TestComponent.class.getName() + "3";
    private final String COMPONENT_4_NAME = TestComponent.class.getName() + "4";
    private final String COMPONENT_5_NAME = TestComponent.class.getName() + "5";
    private final String COMPONENT_6_NAME = TestComponent.class.getName() + "6";

    @Mock
    private ExecutionGraph graph;
    @Mock
    private FlowBuilderContext context;
    @Mock
    private ExecutionNode parentEn;
    @Mock
    private ExecutionNode component1En;
    @Mock
    private ExecutionNode component2En;
    @Mock
    private ExecutionNode component3En;
    @Mock
    private ExecutionNode component4En;
    @Mock
    private ExecutionNode component5En;
    @Mock
    private ExecutionNode component6En;

    private ExecutionNode stopEn = new ExecutionNode(new ReferencePair<>(new Stop()));
    private ExecutionNode routerEn = new ExecutionNode(new ReferencePair<>(new RouterWrapper()));

    @BeforeEach
    void setUp() {
        doReturn(new TestComponent()).when(component1En).getComponent();
        doReturn(new TestComponent()).when(component2En).getComponent();
        doReturn(new TestComponent()).when(component3En).getComponent();
        doReturn(new TestComponent()).when(component4En).getComponent();
        doReturn(new TestComponent()).when(component5En).getComponent();
        doReturn(new TestComponent()).when(component6En).getComponent();

        doReturn(stopEn).when(context).instantiateComponent(Stop.class);
        doReturn(routerEn).when(context).instantiateComponent(RouterWrapper.class.getName());
        doReturn(component1En).when(context).instantiateComponent(COMPONENT_1_NAME);
        doReturn(component2En).when(context).instantiateComponent(COMPONENT_2_NAME);
        doReturn(component3En).when(context).instantiateComponent(COMPONENT_3_NAME);
        doReturn(component4En).when(context).instantiateComponent(COMPONENT_4_NAME);
        doReturn(component5En).when(context).instantiateComponent(COMPONENT_5_NAME);
        doReturn(component6En).when(context).instantiateComponent(COMPONENT_6_NAME);
    }

    @Test
    void shouldCorrectlyHandleRouterComponent() {
        // Given
        JSONArray whenArray = new JSONArray();
        whenArray.put(conditionalBranch("1 == 1", COMPONENT_3_NAME, COMPONENT_1_NAME));
        whenArray.put(conditionalBranch("'hello' == 'hello1'", COMPONENT_2_NAME, COMPONENT_4_NAME));
        whenArray.put(conditionalBranch(DEFAULT_CONDITION, COMPONENT_6_NAME, COMPONENT_5_NAME));

        JSONObject componentDefinition = ComponentsBuilder.forComponent(RouterWrapper.class)
                .with("when", whenArray)
                .build();

        RouterComponentBuilder builder = new RouterComponentBuilder(graph, context);

        // When
        ExecutionNode lastNode = builder.build(parentEn, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(stopEn);

        verify(graph).putEdge(parentEn, routerEn);

        // First condition
        verify(graph).putEdge(routerEn, component3En);
        verify(graph).putEdge(component3En, component1En);
        verify(graph).putEdge(component1En, stopEn);

        // Second condition
        verify(graph).putEdge(routerEn, component2En);
        verify(graph).putEdge(component2En, component4En);
        verify(graph).putEdge(component4En, stopEn);

        // Otherwise
        verify(graph).putEdge(routerEn, component6En);
        verify(graph).putEdge(component6En, component5En);
        verify(graph).putEdge(component5En, stopEn);

        verifyNoMoreInteractions(parentEn);
    }

    private JSONObject conditionalBranch(String condition, String... componentsNames) {
        JSONObject object = new JSONObject();
        object.put("condition", condition);
        object.put("next", ComponentsBuilder.createNextComponentsArray(componentsNames));
        return object;
    }


}
