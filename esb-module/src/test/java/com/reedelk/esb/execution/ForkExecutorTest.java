package com.reedelk.esb.execution;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.component.ForkWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Join;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.component.Stop;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


class ForkExecutorTest extends AbstractExecutionTest {

    private ForkExecutor executor = spy(new ForkExecutor());

    private ExecutionNode forkNode;
    private ExecutionNode fork1Node;
    private ExecutionNode fork2Node;
    private ExecutionNode joinNode;
    private ExecutionNode nodeFollowingJoin;

    @BeforeEach
    void setUp() {
        ForkWrapper forkWrapper = spy(new ForkWrapper());
        doReturn(Schedulers.elastic()).when(forkWrapper).getScheduler();
        doReturn(Schedulers.elastic()).when(executor).flowScheduler();

        forkNode = newExecutionNode(forkWrapper);
        joinNode = newExecutionNode(new JoinString());
        fork1Node = newExecutionNode(new AddPostfixSyncProcessor("-fork1"));
        fork2Node = newExecutionNode(new AddPostfixSyncProcessor("-fork2"));
        nodeFollowingJoin = newExecutionNode(new AddPostfixSyncProcessor("-following-join"));
    }

    @Test
    void shouldForkAndJoinCorrectlyThePayload() {
        // Given
        ExecutionGraph graph = GraphWithForkBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinNode)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("ForkTest-fork1,ForkTest-fork2"))
                .verifyComplete();
    }

    @Test
    void shouldForkAndJoinCorrectlyForAnyMessageInTheStream() {
        // Given
        ExecutionGraph graph = GraphWithForkBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinNode)
                .build();

        MessageAndContext event1 = newEventWithContent("ForkTest1");
        MessageAndContext event2 = newEventWithContent("ForkTest2");
        Publisher<MessageAndContext> publisher = Flux.just(event1, event2);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("ForkTest1-fork1,ForkTest1-fork2"))
                .assertNext(assertMessageContains("ForkTest2-fork1,ForkTest2-fork2"))
                .verifyComplete();
    }

    @Test
    void shouldForkAndJoinCorrectlyAndContinueExecutionUntilTheEndOfTheGraph() {
        // Given
        ExecutionGraph graph = GraphWithForkBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinNode)
                .afterForkSequence(nodeFollowingJoin)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("ForkTest-fork1,ForkTest-fork2-following-join"))
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionAndStopExecutionWhenBranchProcessorThrowsException() {
        // Given
        ExecutionNode processorThrowingException = newExecutionNode(new ProcessorThrowingExceptionSync());

        ExecutionGraph graph = GraphWithForkBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(processorThrowingException)
                .join(joinNode)
                .afterForkSequence(nodeFollowingJoin)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof IllegalStateException);
    }

    @Test
    void shouldThrowExceptionAndStopExecutionWhenJoinProcessorThrowsException() {
        // Given
        ExecutionNode joinThrowingException = newExecutionNode(new JoinThrowingException());

        ExecutionGraph graph = GraphWithForkBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinThrowingException)
                .afterForkSequence(nodeFollowingJoin)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof IllegalStateException);
    }

    @Test
    void shouldThrowExceptionWhenJoinDoesNotImplementJoinInterface() {
        // Given
        ExecutionNode incorrectJoinType = newExecutionNode(new AddPostfixSyncProcessor("incorrect-join"));

        ExecutionGraph graph = GraphWithForkBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(incorrectJoinType)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> executor.execute(publisher, forkNode, graph));

        // Then
        Assertions.assertThat(thrown.getMessage())
                .isEqualTo("Fork must be followed by a component implementing [com.reedelk.runtime.api.component.Join] interface");
    }

    class JoinString implements Join {
        @Override
        public Message apply(List<Message> messages) {
            String joined = messages.stream()
                    .map(message -> message.getContent().asString())
                    .collect(joining(","));
            return MessageBuilder.get().text(joined).build();
        }
    }

    class JoinThrowingException implements Join {
        @Override
        public Message apply(List<Message> messagesToJoin) {
            throw new IllegalStateException("Join not valid");
        }
    }

    static class GraphWithForkBuilder {

        private ExecutionNode fork;
        private ExecutionNode join;
        private ExecutionNode inbound;
        private ComponentDisposer disposer;
        private List<ForkSequence> forkSequenceList = new ArrayList<>();
        private List<ExecutionNode> followingSequence = new ArrayList<>();

        static GraphWithForkBuilder get() {
            return new GraphWithForkBuilder();
        }

        GraphWithForkBuilder fork(ExecutionNode fork) {
            this.fork = fork;
            return this;
        }

        GraphWithForkBuilder join(ExecutionNode join) {
            this.join = join;
            return this;
        }

        GraphWithForkBuilder inbound(ExecutionNode inbound) {
            this.inbound = inbound;
            return this;
        }

        GraphWithForkBuilder disposer(ComponentDisposer disposer) {
            this.disposer = disposer;
            return this;
        }

        GraphWithForkBuilder forkSequence(ExecutionNode... sequence) {
            this.forkSequenceList.add(new ForkSequence(sequence));
            return this;
        }

        GraphWithForkBuilder afterForkSequence(ExecutionNode... afterForkSequence) {
            this.followingSequence = Arrays.asList(afterForkSequence);
            return this;
        }

        ExecutionGraph build() {
            ExecutionGraph graph = ExecutionGraph.build();
            graph.putEdge(null, inbound);
            graph.putEdge(inbound, fork);

            ExecutionNode endOfFork = newExecutionNode(disposer, new Stop());

            ForkWrapper forkWrapper = (ForkWrapper) fork.getComponent();
            forkWrapper.setStopNode(endOfFork);
            for (ForkSequence sequence : forkSequenceList) {
                buildSequence(graph, fork, endOfFork, sequence.sequence);
                if (sequence.sequence.size() > 0) {
                    forkWrapper.addForkNode(sequence.sequence.get(0));
                }
            }

            ExecutionNode last = endOfFork;

            if (join != null) {
                graph.putEdge(endOfFork, join);
                last = join;
            }

            ExecutionNode endOfGraph = newExecutionNode(disposer, new Stop());
            if (followingSequence.size() > 0) {
                buildSequence(graph, last, endOfGraph, followingSequence);
            } else {
                graph.putEdge(last, endOfGraph);
            }

            return graph;
        }

        private void buildSequence(ExecutionGraph graph, ExecutionNode root, ExecutionNode end, List<ExecutionNode> sequence) {
            ExecutionNode previous = root;
            for (ExecutionNode node : sequence) {
                graph.putEdge(previous, node);
                previous = node;
            }
            graph.putEdge(previous, end);
        }

        class ForkSequence {
            List<ExecutionNode> sequence;

            ForkSequence(ExecutionNode[] sequence) {
                this.sequence = Arrays.asList(sequence);
            }
        }
    }
}
