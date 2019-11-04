package com.reedelk.esb.execution;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.component.TryCatchWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.component.Stop;
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
        tryWithException = newExecutionNode(new ProcessorThrowingExceptionSync(exceptionMessage));
    }

    @Test
    void shouldExecuteTryFlow() {
        // Given
        ExecutionGraph graph = GraphWithTryCatchBuilder.get()
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
        ExecutionGraph graph = GraphWithTryCatchBuilder.get()
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

    class CatchSyncProcessor implements ProcessorSync {
        @Override
        public Message apply(Message message, FlowContext flowContext) {
            Exception thrown = (Exception) message.getContent().data();
            String outputString = thrown.getMessage();
            return MessageBuilder.get().text(outputString).build();
        }
    }

    static class GraphWithTryCatchBuilder {

        private ExecutionNode tryNode;
        private ExecutionNode inbound;
        private ExecutionNode catchNode;
        private ExecutionNode tryCatchNode;

        private ComponentDisposer disposer;

        static GraphWithTryCatchBuilder get() {
            return new GraphWithTryCatchBuilder();
        }

        GraphWithTryCatchBuilder inbound(ExecutionNode inbound) {
            this.inbound = inbound;
            return this;
        }

        GraphWithTryCatchBuilder tryNode(ExecutionNode tryNode) {
            this.tryNode = tryNode;
            return this;
        }

        GraphWithTryCatchBuilder catchNode(ExecutionNode catchNode) {
            this.catchNode = catchNode;
            return this;
        }

        GraphWithTryCatchBuilder disposer(ComponentDisposer disposer) {
            this.disposer = disposer;
            return this;
        }

        GraphWithTryCatchBuilder tryCatchNode(ExecutionNode tryCatchNode) {
            this.tryCatchNode = tryCatchNode;
            return this;
        }

        ExecutionGraph build() {
            ExecutionGraph graph = ExecutionGraph.build();
            graph.putEdge(null, inbound);
            graph.putEdge(inbound, tryCatchNode);
            graph.putEdge(tryCatchNode, tryNode);
            graph.putEdge(tryCatchNode, catchNode);

            ExecutionNode endOfTryCatch = newExecutionNode(disposer, new Stop());
            TryCatchWrapper tryCatchWrapper = (TryCatchWrapper) tryCatchNode.getComponent();
            tryCatchWrapper.setStopNode(endOfTryCatch);
            tryCatchWrapper.setFirstTryNode(tryNode);
            tryCatchWrapper.setFirstCatchNode(catchNode);

            graph.putEdge(tryNode, endOfTryCatch);
            graph.putEdge(catchNode, endOfTryCatch);

            ExecutionNode endOfGraphNode = newExecutionNode(disposer, new Stop());
            graph.putEdge(endOfTryCatch, endOfGraphNode);
            return graph;
        }
    }
}
