package com.reedelk.esb.execution;

import com.reedelk.esb.component.RouterWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.component.Stop;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class RouterExecutorTest extends AbstractExecutionTest {

    private RouterExecutor executor = new RouterExecutor();
    private ExecutionNode endOfChoice = newExecutionNode(new Stop());

    @Test
    void shouldExecuteCorrectBranch() {
        // Given
        ExecutionNode route1Node = newExecutionNode(new AddPostfixSyncProcessor("-route1"));
        ExecutionNode route2Node = newExecutionNode(new AddPostfixSyncProcessor("-route2"));

        RouterWrapper routerComponent = new RouterWrapper();
        ExecutionNode routerNode = newExecutionNode(routerComponent);

        routerComponent.setEndOfRouterStopNode(endOfChoice);
        routerComponent.addPathExpressionPair("payload == 'Route1'", route1Node);
        routerComponent.addPathExpressionPair("payload == 'Route2'", route2Node);

        ExecutionNode nodeFollowingStop = newExecutionNode(new AddPostfixSyncProcessor("-following-stop"));

        ExecutionGraph graph = ExecutionGraph.build();
        graph.putEdge(null, inbound);
        graph.putEdge(inbound, routerNode);
        graph.putEdge(routerNode, route1Node);
        graph.putEdge(routerNode, route2Node);
        graph.putEdge(route1Node, endOfChoice);
        graph.putEdge(route2Node, endOfChoice);
        graph.putEdge(endOfChoice, nodeFollowingStop);
        graph.putEdge(nodeFollowingStop, stop);

        EventContext route2Event = newEventWithContent("Route2");
        EventContext route1Event = newEventWithContent("Route1");

        Publisher<EventContext> publisher = Flux.just(route2Event, route1Event);

        // When
        Publisher<EventContext> endPublisher =
                executor.execute(publisher, routerNode, graph);

        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("Route2-route2-following-stop"))
                .assertNext(assertMessageContains("Route1-route1-following-stop"))
                .verifyComplete();
    }
}
