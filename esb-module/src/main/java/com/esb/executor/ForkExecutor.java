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
    public ExecutionResult execute(ExecutionNode executionNode, final Message message, ExecutionGraph graph) {

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

        Collection<ExecutionNode> followingNodes = graph.successors(stopNode);

        ExecutionNode joinExecutionNode = checkAtLeastOneAndGetOrThrow(
                followingNodes.stream(),
                "Stop node from fork must be followed by one node");

        Component component = joinExecutionNode.getComponent();
        checkState(component instanceof Join, "Fork must be followed by Join component");

        Join join = (Join) component;
        Message joinedMessage = join.apply(results);

        Collection<ExecutionNode> followingExecutionNodes = graph.successors(joinExecutionNode);

        ExecutionNode nextOfJoin = checkAtLeastOneAndGetOrThrow(
                followingExecutionNodes.stream(),
                "Join must be followed by one node");


        return Executors.execute(nextOfJoin, joinedMessage, graph);
    }

}
