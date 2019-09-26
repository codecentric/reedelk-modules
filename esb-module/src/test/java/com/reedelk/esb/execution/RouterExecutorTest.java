package com.reedelk.esb.execution;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.component.RouterWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.script.DynamicBoolean;
import com.reedelk.runtime.component.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class RouterExecutorTest extends AbstractExecutionTest {

    private RouterExecutor executor = new RouterExecutor();

    private ExecutionNode routerNode;
    private ExecutionNode route1Node;
    private ExecutionNode route2Node;
    private ExecutionNode nodeFollowingStop;

    @BeforeEach
    void setUp() {
        routerNode = newExecutionNode(new RouterWrapper());
        route1Node = newExecutionNode(new AddPostfixSyncProcessor("-route1"));
        route2Node = newExecutionNode(new AddPostfixSyncProcessor("-route2"));
        nodeFollowingStop = newExecutionNode(new AddPostfixSyncProcessor("-following-stop"));
    }

    @Test
    void shouldExecuteCorrectBranchForGivenCondition() {
        // Given
        ExecutionGraph graph = GraphWithRouterBuilder.get()
                .router(routerNode)
                .disposer(disposer)
                .inbound(inbound)
                .conditionWithSequence("#[payload == 'Route1']", route1Node)
                .conditionWithSequence("#[payload == 'Route2']", route2Node)
                .afterRouterSequence(nodeFollowingStop)
                .build();

        MessageAndContext event = newEventWithContent("Route2");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, routerNode, graph);

        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("Route2-route2-following-stop"))
                .verifyComplete();
    }

    @Test
    void shouldExecuteDefaultPath() {
        // Given
        ExecutionNode route3Node = newExecutionNode(new AddPostfixSyncProcessor("-otherwise"));
        ExecutionGraph graph = GraphWithRouterBuilder.get()
                .router(routerNode)
                .disposer(disposer)
                .inbound(inbound)
                .conditionWithSequence("#[payload == 'Route1']", route1Node)
                .conditionWithSequence("#[payload == 'Route2']", route2Node)
                .conditionWithSequence("otherwise", route3Node)
                .afterRouterSequence(nodeFollowingStop)
                .build();

        MessageAndContext event = newEventWithContent("RouteOtherwise");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, routerNode, graph);

        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("RouteOtherwise-otherwise-following-stop"))
                .verifyComplete();
    }

    @Test
    void shouldExecuteUntilEndOfBranchWhenNoNodesAfterRouter() {
        // Given
        ExecutionGraph graph = GraphWithRouterBuilder.get()
                .router(routerNode)
                .disposer(disposer)
                .inbound(inbound)
                .conditionWithSequence("#[payload == 'Route1']", route1Node)
                .conditionWithSequence("#[payload == 'Route2']", route2Node)
                .build();

        MessageAndContext event = newEventWithContent("Route1");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, routerNode, graph);

        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("Route1-route1"))
                .verifyComplete();
    }

    @Test
    void shouldExecuteCorrectBranchForAnyMessageInTheStream() {
        // Given
        ExecutionGraph graph = GraphWithRouterBuilder.get()
                .router(routerNode)
                .disposer(disposer)
                .inbound(inbound)
                .conditionWithSequence("#[payload == 'Route1']", route1Node)
                .conditionWithSequence("#[payload == 'Route2']", route2Node)
                .afterRouterSequence(nodeFollowingStop)
                .build();

        MessageAndContext route2Event = newEventWithContent("Route2");
        MessageAndContext route1Event = newEventWithContent("Route1");

        Publisher<MessageAndContext> publisher = Flux.just(route2Event, route1Event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, routerNode, graph);

        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("Route2-route2-following-stop"))
                .assertNext(assertMessageContains("Route1-route1-following-stop"))
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionAndStopExecutionWhenBranchProcessorThrowsException() {
        // Given
        ExecutionNode processorThrowingException = newExecutionNode(new ProcessorThrowingExceptionSync());

        ExecutionGraph graph = GraphWithRouterBuilder.get()
                .router(routerNode)
                .disposer(disposer)
                .inbound(inbound)
                .conditionWithSequence("#[payload == 'Route1']", processorThrowingException)
                .conditionWithSequence("#[payload == 'Route2']", route2Node)
                .afterRouterSequence(nodeFollowingStop)
                .build();

        MessageAndContext event = newEventWithContent("Route1");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, routerNode, graph);

        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof IllegalStateException);
    }

    static class GraphWithRouterBuilder {

        private ComponentDisposer disposer;
        private ExecutionNode router;
        private ExecutionNode inbound;
        private List<ExecutionNode> followingSequence = new ArrayList<>();
        private List<ConditionWithSequence> conditionWithSequences = new ArrayList<>();

        static GraphWithRouterBuilder get() {
            return new GraphWithRouterBuilder();
        }

        GraphWithRouterBuilder router(ExecutionNode router) {
            this.router = router;
            return this;
        }

        GraphWithRouterBuilder inbound(ExecutionNode inbound) {
            this.inbound = inbound;
            return this;
        }

        GraphWithRouterBuilder disposer(ComponentDisposer disposer) {
            this.disposer = disposer;
            return this;
        }

        GraphWithRouterBuilder conditionWithSequence(String condition, ExecutionNode... sequence) {
            conditionWithSequences.add(new ConditionWithSequence(condition, sequence));
            return this;
        }

        GraphWithRouterBuilder afterRouterSequence(ExecutionNode... following) {
            this.followingSequence = Arrays.asList(following);
            return this;
        }

        ExecutionGraph build() {
            ExecutionGraph graph = ExecutionGraph.build();
            graph.putEdge(null, inbound);
            graph.putEdge(inbound, router);

            ExecutionNode endOfRouter = newExecutionNode(disposer, new Stop());

            RouterWrapper routerWrapper = (RouterWrapper) router.getComponent();
            routerWrapper.setEndOfRouterStopNode(endOfRouter);
            for (ConditionWithSequence item : conditionWithSequences) {
                if (item.sequence.size() > 0) {
                    routerWrapper.addExpressionAndPathPair(DynamicBoolean.from(item.condition), item.sequence.get(0));
                    buildSequence(graph, router, endOfRouter, item.sequence);
                }
            }

            ExecutionNode endOfGraph = newExecutionNode(disposer, new Stop());
            if (followingSequence.size() > 0) {
                buildSequence(graph, endOfRouter, endOfGraph, followingSequence);
            } else {
                graph.putEdge(endOfRouter, endOfGraph);
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

        class ConditionWithSequence {
            String condition;
            List<ExecutionNode> sequence;
            ConditionWithSequence(String condition, ExecutionNode[] sequence) {
                this.sequence = Arrays.asList(sequence);
                this.condition = condition;
            }
        }
    }
}
