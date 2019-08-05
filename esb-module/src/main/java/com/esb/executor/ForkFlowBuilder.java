package com.esb.executor;

import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class ForkFlowBuilder implements FlowBuilder {


    private static ExecutionNode getNextNodeOrThrow(ExecutionGraph graph, ExecutionNode node, String message) {
        Collection<ExecutionNode> successors = graph.successors(node);
        return checkAtLeastOneAndGetOrThrow(successors.stream(), message);
    }


    @Override
    public Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {
        /**
        ForkWrapper fork = (ForkWrapper) executionNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = fork.apply(message);

        List<Callable<Message>> allTasks = new ArrayList<>();

        Function<ExecutionNode, Callable<Message>> mapper = currentExecutionNode ->
         ExecutionFlowBuilder.build(currentExecutionNode,
                        SerializationUtils.clone(message), graph)::getMessage;

        for (ExecutionNode nextExecutionNode : nextExecutionNodes) {
            Callable<Message> messageCallable = mapper.apply(nextExecutionNode);
            allTasks.add(messageCallable);
        }

        List<Message> results = fork.invokeAllAndWait(allTasks);

        ExecutionNode stopNode = fork.getStopNode();

        ExecutionNode joinExecutionNode = getNextNodeOrThrow(graph, stopNode,
                "Fork stop node must be followed by one node");

        Component joinComponent = joinExecutionNode.getComponent();
        checkState(joinComponent instanceof Join,
                String.format("Fork must be followed by a component implementing %s interface", Join.class.getName()));

        Join join = (Join) joinComponent;
        Message joinedMessage = join.apply(results);


        Collection<ExecutionNode> successors = graph.successors(joinExecutionNode);
        if (successors.isEmpty()) {
            // The join was the last node of the graph.
            return new ExecutionResult(joinedMessage, joinExecutionNode);

        } else {
            ExecutionNode nextOfJoin = getNextNodeOrThrow(graph, joinExecutionNode,
                    "Join must be followed by one node");
         return ExecutionFlowBuilder.build(nextOfJoin, joinedMessage, graph);
         }**/
        return parentFlux.flatMap(context -> Flux.empty());
    }
}
