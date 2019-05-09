package com.esb.executor;

import com.esb.api.component.Component;
import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.component.ForkWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import com.esb.internal.commons.SerializationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;
import static com.esb.commons.Preconditions.checkState;

public class ForkExecutor implements Executor {

    @Override
    public ExecutionResult execute(final ExecutionNode executionNode, final Message message, final ExecutionGraph graph) {

        ForkWrapper fork = (ForkWrapper) executionNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = fork.apply(message);

        List<Callable<Message>> allTasks = new ArrayList<>();

        Function<ExecutionNode, Callable<Message>> mapper = currentExecutionNode ->
                Executors.execute(currentExecutionNode,
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
            return Executors.execute(nextOfJoin, joinedMessage, graph);
        }
    }

    private static ExecutionNode getNextNodeOrThrow(ExecutionGraph graph, ExecutionNode node, String message) {
        Collection<ExecutionNode> successors = graph.successors(node);
        return checkAtLeastOneAndGetOrThrow(successors.stream(), message);
    }


}
