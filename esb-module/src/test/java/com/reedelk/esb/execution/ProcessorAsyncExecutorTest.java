package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessorAsyncExecutorTest extends AbstractExecutionTest {

    private ProcessorAsyncExecutor executor;

    @BeforeEach
    void setUp() {
        executor = spy(new ProcessorAsyncExecutor());
        doReturn(Schedulers.elastic()).when(executor).flowScheduler();
        doReturn(Optional.of(500L)).when(executor).asyncCallbackTimeout();
    }

    @Test
    void shouldCorrectlyApplyProcessorAsyncToMessage() {
        // Given
        ExecutionNode processor = newExecutionNode(new AddPostfixAsync("-async"));

        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);

        EventContext event = newEventWithContent("input");
        Publisher<EventContext> publisher = Mono.just(event);

        // When
        Publisher<EventContext> endPublisher =
                executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("input-async"))
                .verifyComplete();
    }

    @Test
    void shouldCorrectlyApplyProcessorAsyncToEachMessageInTheStream() {
        // Given
        ExecutionNode processor = newExecutionNode(new AddPostfixAsync("-async"));

        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);
        EventContext event1 = newEventWithContent("input1");
        EventContext event2 = newEventWithContent("input2");
        Publisher<EventContext> publisher = Flux.just(event1, event2);

        // When
        Publisher<EventContext> endPublisher =
                executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContainsOneOf("input1-async", "input2-async"))
                .assertNext(assertMessageContainsOneOf("input1-async", "input2-async"))
                .verifyComplete();
    }

    @Test
    void shouldCorrectlyThrowErrorWhenProcessorAsyncThrowsException() {
        // Given
        ExecutionNode processor = newExecutionNode(new ProcessorThrowingExceptionAsync());
        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);
        EventContext inputEventContext = newEventWithContent("input");

        Publisher<EventContext> publisher = Flux.just(inputEventContext);

        // When
        Publisher<EventContext> endPublisher = executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof IllegalStateException);
    }

    @Test
    void shouldCorrectlyThrowTimeoutErrorWhenProcessorAsyncWaitsTooLong() {
        // Given
        ExecutionNode processor = newExecutionNode(new ProcessorAsyncTakingTooLong());
        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);
        EventContext inputEventContext = newEventWithContent("input");

        Publisher<EventContext> publisher = Flux.just(inputEventContext);

        // When
        Publisher<EventContext> endPublisher = executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof TimeoutException);
    }

    // If the processor is the last node, then it must be present a Stop node.
    // If a Stop node is not there, it means there has been an error while
    // building the graph.
    @Test
    void shouldThrowExceptionIfProcessorAsyncNotFollowedByAnyOtherNode() {
        // Given
        ExecutionNode processor = newExecutionNode(new AddPostfixAsync("-async"));
        ExecutionGraph graph = newGraphSequence(inbound, processor);

        // When
        Assertions.assertThrows(IllegalStateException.class, () ->
                        executor.execute(Flux.just(), processor, graph),
                "Expected processor sync to be followed by one node");
    }

    class ProcessorAsyncTakingTooLong implements ProcessorAsync {
        @Override
        public void apply(Message input, OnResult callback) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // nothing to do
                }
                callback.onResult(MessageBuilder.get().text("hello").build());
            });
        }
    }

    class ProcessorThrowingExceptionAsync implements ProcessorAsync {
        @Override
        public void apply(Message input, OnResult callback) {
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // nothing to do
                }
                callback.onError(new IllegalStateException("Error"));
            }).start();
        }
    }

    class AddPostfixAsync implements ProcessorAsync {

        private String postfix;

        AddPostfixAsync(String postfix) {
            this.postfix = postfix;
        }

        @Override
        public void apply(Message input, OnResult callback) {
            new Thread(() -> {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // nothing to do
                }
                String inputString = input.getTypedContent().asString();
                String outputString = inputString + postfix;
                Message out = MessageBuilder.get().text(outputString).build();
                callback.onResult(out);
            }).start();
        }
    }
}
