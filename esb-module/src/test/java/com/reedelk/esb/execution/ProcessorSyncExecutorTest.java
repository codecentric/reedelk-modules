package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.test.utils.TestInboundComponent;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.component.Stop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;


class ProcessorSyncExecutorTest {

    private ProcessorSyncExecutor executor = new ProcessorSyncExecutor();

    private ExecutionGraph graph = ExecutionGraph.build();

    private ExecutionNode inboundExecutionNode;
    private ExecutionNode stopExecutionNode;

    @BeforeEach
    void setUp() {
        stopExecutionNode = new ExecutionNode(new ExecutionNode.ReferencePair<>(new Stop()));
        inboundExecutionNode = new ExecutionNode(new ExecutionNode.ReferencePair<>(new TestInboundComponent()));
        graph.putEdge(null, inboundExecutionNode);
    }

    @Test
    void shouldCorrectlyApplyProcessorToMessage() {
        // Given
        ExecutionNode processor = newExecutionNode(new TestSyncProcessor());
        graph.putEdge(inboundExecutionNode, processor);
        graph.putEdge(processor, stopExecutionNode);

        Message originalMessage = MessageBuilder.get().text("inputContent").build();

        EventContext event = new NoActionResultEventContext(originalMessage);

        Mono<EventContext> publisher = Mono.just(event);

        // When
        Publisher<EventContext> endPublisher = executor.execute(publisher, processor, graph);

        // Then
        String expectedOutput = "inputContent-postfix";
        StepVerifier.create(endPublisher)
                .assertNext(messageContext -> {

                    String out = (String) messageContext.getMessage().getTypedContent().content();

                    assertThat(out).isEqualTo(expectedOutput);

                }).verifyComplete();
    }

    @Test
    void shouldCorrectlyThrowErrorWhenProcessorThrowsException() {
        // Given
        ExecutionNode processor = newExecutionNode(new TestSyncProcessorThrowingException());
        graph.putEdge(inboundExecutionNode, processor);
        graph.putEdge(processor, stopExecutionNode);

        Message originalMessage = MessageBuilder.get().text("input").build();

        OnResultVerifier onResultVerifier = new OnResultVerifier();
        EventContext inputEventContext = new EventContext(originalMessage, onResultVerifier);

        Publisher<EventContext> publisher = Flux.just(inputEventContext);

        // When
        Publisher<EventContext> endPublisher = executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher).verifyError();
    }

    // If the processor is the last node, then it must be present a Stop node.
    // If a Stop node is not there, it means there has been an error while
    // building the graph.
    @Test
    void shouldThrowExceptionIfProcessorNotFollowedByAnyOtherNode() {
        // Given
        ExecutionNode processor = newExecutionNode(new TestSyncProcessor());
        graph.putEdge(inboundExecutionNode, processor);

        // When
        Assertions.assertThrows(IllegalStateException.class, () ->
                        executor.execute(Flux.just(), processor, graph),
                "Expected processor sync to be followed by one node");
    }

    private ExecutionNode newExecutionNode(Component component) {
        return new ExecutionNode(new ExecutionNode.ReferencePair<>(component));
    }

    private class EmptyResult implements OnResult {
    }

    private class NoActionResultEventContext extends EventContext {
        NoActionResultEventContext(Message message) {
            super(message, new EmptyResult());
        }
    }

    private class TestSyncProcessor implements ProcessorSync {
        @Override
        public Message apply(Message input) {
            String inputString = (String) input.getTypedContent().content();
            String outputString = inputString + "-postfix";
            return MessageBuilder.get()
                    .mimeType(MimeType.TEXT)
                    .content(outputString)
                    .build();
        }
    }

    private class TestSyncProcessorThrowingException implements ProcessorSync {
        @Override
        public Message apply(Message input) {
            throw new IllegalStateException("Input not valid");
        }
    }

    class OnResultVerifier implements OnResult {
        Throwable throwable;

        @Override
        public void onError(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}