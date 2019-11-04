package com.reedelk.esb.execution;

import com.reedelk.esb.component.TryCatchWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.spy;

class TryCatchExecutorTest extends AbstractExecutionTest {

    private final String exceptionMessage = "TryCatch-Exception thrown";

    private TryCatchExecutor executor = new TryCatchExecutor();

    private ExecutionNode tryNode;
    private ExecutionNode catchNode;
    private ExecutionNode tryCatchNode;
    private ExecutionNode tryWithException;

    @BeforeEach
    void setUp() {
        TryCatchWrapper tryCatchWrapper = spy(new TryCatchWrapper());
        tryCatchNode = newExecutionNode(tryCatchWrapper);
        tryNode = newExecutionNode(new AddPostfixSyncProcessor("-try"));
        catchNode = newExecutionNode(new CatchSyncProcessor());
        tryWithException = newExecutionNode(new ProcessorThrowingIllegalStateExceptionSync(exceptionMessage));
    }

    @Test
    void shouldExecuteTryFlow() {
        // Given
        ExecutionGraph graph = TryCatchTestGraphBuilder.get()
                .inbound(inbound)
                .tryNode(tryNode)
                .disposer(disposer)
                .catchNode(catchNode)
                .tryCatchNode(tryCatchNode)
                .build();

        MessageAndContext event = newEventWithContent("TryCatchTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, tryCatchNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("TryCatchTest-try"))
                .verifyComplete();
    }

    @Test
    void shouldExecuteCatchFlow() {
        // Given
        ExecutionGraph graph = TryCatchTestGraphBuilder.get()
                .inbound(inbound)
                .disposer(disposer)
                .catchNode(catchNode)
                .tryNode(tryWithException)
                .tryCatchNode(tryCatchNode)
                .build();

        MessageAndContext event = newEventWithContent("TryCatchTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, tryCatchNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains(exceptionMessage))
                .verifyComplete();
    }

    @Test
    void shouldExecuteFlowFollowingForkNodeAfterTryFlow() {
        // Given
        String expectedExceptionThrown = "inner exception";
        ExecutionNode afterTryCatchNode = newExecutionNode(new AddPostfixSyncProcessor("-afterTryCatchNode"));

        ExecutionNode exceptionThrownInsideCatchFlow = newExecutionNode(new ProcessorThrowingIllegalStateExceptionSync(expectedExceptionThrown));
        ExecutionGraph graph = TryCatchTestGraphBuilder.get()
                .inbound(inbound)
                .tryNode(tryWithException)
                .disposer(disposer)
                .catchNode(exceptionThrownInsideCatchFlow)
                .tryCatchNode(tryCatchNode)
                .afterTryCatchSequence(afterTryCatchNode)
                .build();

        MessageAndContext event = newEventWithContent("TryCatchTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, tryCatchNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                expectedExceptionThrown.equals(throwable.getMessage())).verify();
    }

    @Test
    void shouldExecuteFlowFollowingForkNodeAfterCatchFlow() {
        // Given
        ExecutionNode afterTryCatchNode = newExecutionNode(new AddPostfixSyncProcessor("-afterTryCatchNode"));
        ExecutionGraph graph = TryCatchTestGraphBuilder.get()
                .inbound(inbound)
                .disposer(disposer)
                .catchNode(catchNode)
                .tryNode(tryWithException)
                .tryCatchNode(tryCatchNode)
                .afterTryCatchSequence(afterTryCatchNode)
                .build();

        MessageAndContext event = newEventWithContent("TryCatchTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, tryCatchNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains(exceptionMessage + "-afterTryCatchNode"))
                .verifyComplete();
    }

    @Test
    void shouldRethrowExceptionWhenExceptionThrownInsideCatchFlow() {
        // Given
        ExecutionNode afterTryCatchNode = newExecutionNode(new AddPostfixSyncProcessor("-afterTryCatchNode"));
        ExecutionGraph graph = TryCatchTestGraphBuilder.get()
                .inbound(inbound)
                .disposer(disposer)
                .catchNode(catchNode)
                .tryNode(tryWithException)
                .tryCatchNode(tryCatchNode)
                .afterTryCatchSequence(afterTryCatchNode)
                .build();

        MessageAndContext event = newEventWithContent("TryCatchTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, tryCatchNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains(exceptionMessage + "-afterTryCatchNode"))
                .verifyComplete();
    }

    class CatchSyncProcessor implements ProcessorSync {
        @Override
        public Message apply(Message message, FlowContext flowContext) {
            Exception thrown = (Exception) message.getContent().data();
            String outputString = thrown.getMessage();
            return MessageBuilder.get().text(outputString).build();
        }
    }
}
