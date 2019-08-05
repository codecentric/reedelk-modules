package com.esb.executor;

import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;

public class RouterFlowBuilder implements FlowBuilder {


    @Override
    public Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {
        /**
        RouterWrapper router = (RouterWrapper) executionNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = router.apply(message);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(
                nextExecutionNodes.stream(),
                "Router must be followed by exactly one node");

        // This one stops until it finds stop. Then we need to keep going
         ExecutionResult build = ExecutionFlowBuilder.build(next, message, graph);

         Collection<ExecutionNode> nextNode = graph.successors(build.getLastExecutedNode());
        checkState(nextExecutionNodes.size() == 1, "Outgoing execution Nodes for Stop must have size 1");

        Optional<ExecutionNode> moreNodeAfterStop = nextNode.stream().findFirst();

        return moreNodeAfterStop.isPresent() ?
         ExecutionFlowBuilder.build(moreNodeAfterStop.get(), build.getMessage(), graph) :
         build;
         */
        return parentFlux.flatMap(context -> Flux.empty());
    }
}
