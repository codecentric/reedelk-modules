package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.test.utils.MockFlowBuilderContext;
import com.reedelk.esb.test.utils.TestComponent;
import com.reedelk.runtime.component.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AbstractDeserializerTest {

    final String component1Name = TestComponent.class.getName() + "1";
    final String component2Name = TestComponent.class.getName() + "2";
    final String component3Name = TestComponent.class.getName() + "3";
    final String component4Name = TestComponent.class.getName() + "4";
    final String component5Name = TestComponent.class.getName() + "5";
    final String component6Name = TestComponent.class.getName() + "6";

    @Mock
    protected ExecutionGraph graph;
    @Mock
    protected ComponentDisposer disposer;
    @Spy
    protected MockFlowBuilderContext context;
    @Mock
    protected ExecutionNode parent;
    @Mock
    protected ExecutionNode component1;
    @Mock
    protected ExecutionNode component2;
    @Mock
    protected ExecutionNode component3;
    @Mock
    protected ExecutionNode component4;
    @Mock
    protected ExecutionNode component5;
    @Mock
    protected ExecutionNode component6;

    ExecutionNode stopExecutionNode = new ExecutionNode(disposer, new ExecutionNode.ReferencePair<>(new Stop()));

    @BeforeEach
    void setUp() {
        lenient().doReturn(new TestComponent()).when(component1).getComponent();
        lenient().doReturn(new TestComponent()).when(component2).getComponent();
        lenient().doReturn(new TestComponent()).when(component3).getComponent();
        lenient().doReturn(new TestComponent()).when(component4).getComponent();
        lenient().doReturn(new TestComponent()).when(component5).getComponent();
        lenient().doReturn(new TestComponent()).when(component6).getComponent();

        lenient().doReturn(stopExecutionNode).when(context).instantiateComponent(Stop.class);
        lenient().doReturn(component1).when(context).instantiateComponent(component1Name);
        lenient().doReturn(component2).when(context).instantiateComponent(component2Name);
        lenient().doReturn(component3).when(context).instantiateComponent(component3Name);
        lenient().doReturn(component4).when(context).instantiateComponent(component4Name);
        lenient().doReturn(component5).when(context).instantiateComponent(component5Name);
        lenient().doReturn(component6).when(context).instantiateComponent(component6Name);
    }
}