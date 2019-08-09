package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.OnResult;
import com.esb.api.component.ProcessorAsync;
import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.component.ForkWrapper;
import com.esb.component.RouterWrapper;
import com.esb.system.component.Stop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowExecutorFactoryTest {

    @Test
    void shouldGetComponentBuilderOrThrowReturnCorrectlyForProcessorSync() {
        // Given
        Component component = new TestProcessorSync();

        // Expect
        assertBuilderForTargetClassIs(component, ProcessorSyncFlowExecutor.class);
    }

    @Test
    void shouldGetComponentBuilderOrThrowReturnCorrectlyForProcessorAsync() {
        // Given
        Component component = new TestProcessorAsync();

        // Expect
        assertBuilderForTargetClassIs(component, ProcessorAsyncFlowExecutor.class);
    }

    @Test
    void shouldGetComponentBuilderOrThrowReturnCorrectlyForStop() {
        // Given
        Component component = new Stop();

        // Expect
        assertBuilderForTargetClassIs(component, StopFlowExecutor.class);
    }

    @Test
    void shouldGetComponentBuilderOrThrowReturnCorrectlyForFork() {
        // Given
        Component component = new ForkWrapper();

        // Expect
        assertBuilderForTargetClassIs(component, ForkFlowExecutor.class);
    }

    @Test
    void shouldGetComponentBuilderOrThrowReturnCorrectlyForRouter() {
        // Given
        Component component = new RouterWrapper();

        // Expect
        assertBuilderForTargetClassIs(component, RouterFlowExecutor.class);
    }

    @Test
    void shouldGetComponentBuilderOrThrowThrowExceptionWhenComponentDoesNotImplementKnownInterface() {
        // Given
        Component component = new UndefinedComponentType();

        Assertions.assertThrows(IllegalStateException.class, () ->
                FlowExecutorFactory.get().getComponentBuilderOrThrow(component));
    }

    private void assertBuilderForTargetClassIs(Component component, Class<? extends FlowExecutor> builderClass) {
        // When
        FlowExecutor builder = FlowExecutorFactory.get()
                .getComponentBuilderOrThrow(component);

        // Expect
        assertThat(builder).isNotNull();
        assertThat(builder).isInstanceOf(builderClass);
    }

    interface NotRelatedInterface {
    }

    private class UndefinedComponentType implements Component {

    }

    class TestProcessorAsync implements ProcessorAsync, NotRelatedInterface {
        @Override
        public void apply(Message input, OnResult callback) {
        }
    }

    class TestProcessorSync implements ProcessorSync, NotRelatedInterface {
        @Override
        public Message apply(Message input) {
            return null;
        }
    }
}