package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.component.TryCatchWrapper;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.test.utils.ComponentsBuilder;
import com.reedelk.runtime.component.TryCatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class TryCatchDeserializerTest extends AbstractDeserializerTest {

    private ExecutionNode tryCatchExecutionNode = new ExecutionNode(disposer, new ExecutionNode.ReferencePair<>(new TryCatchWrapper()));

    private TryCatchDeserializer deserializer;

    @BeforeEach
    public void setUp() {
        super.setUp();
        Mockito.doReturn(tryCatchExecutionNode).when(context).instantiateComponent(TryCatch.class.getName());
        deserializer = new TryCatchDeserializer(graph, context);
    }

    @Test
    void shouldCorrectlyDeserializeTryCatchComponent() {
        // Given
        JSONArray tryArray = ComponentsBuilder.createNextComponentsArray(component1Name, component2Name);
        JSONArray catchArray = ComponentsBuilder.createNextComponentsArray(component3Name, component4Name);
        JSONObject componentDefinition = ComponentsBuilder.forComponent(TryCatch.class)
                .with("try", tryArray)
                .with("catch", catchArray)
                .build();

        // When
        ExecutionNode lastNode = deserializer.deserialize(parent, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(stopExecutionNode);

        verify(graph).putEdge(parent, tryCatchExecutionNode);
        verify(graph).putEdge(tryCatchExecutionNode, component1);
        verify(graph).putEdge(tryCatchExecutionNode, component3);

        verify(graph).putEdge(component1, component2);
        verify(graph).putEdge(component3, component4);
        verify(graph).putEdge(component2, stopExecutionNode);
        verify(graph).putEdge(component4, stopExecutionNode);
    }
}
