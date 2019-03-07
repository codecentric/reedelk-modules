package com.esb.executor;

import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.commons.Graph;
import com.esb.component.Fork;
import com.esb.flow.ExecutionNode;
import com.esb.internal.api.commons.SerializationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class ForkExecutor implements Executor {

    @Override
    public ExecutionResult execute(ExecutionNode executionNode, final Message message, Graph graph) {

        Fork fork = (Fork) executionNode.getComponent();
        List<ExecutionNode> nextExecutionNodes = fork.apply(message);


        List<Callable<Message>> allTasks = new ArrayList<>();
        Function<ExecutionNode, Callable<Message>> mapper = en ->
                Executors.execute(en, SerializationUtils.clone(message), graph)::getMessage;

        for (ExecutionNode nextExecutionNode : nextExecutionNodes) {
            Callable<Message> messageCallable = mapper.apply(nextExecutionNode);
            allTasks.add(messageCallable);
        }

        List<Message> results = fork.invokeAllAndWait(allTasks);

        ExecutionNode joinComponentExecutionNode = fork.getJoin();
        Join join = (Join) joinComponentExecutionNode.getComponent();
        Message joinedMessage = join.apply(results);

        Set<ExecutionNode> followingExecutionNodes = graph.successors(joinComponentExecutionNode);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(
                followingExecutionNodes.stream(),
                "Join must be followed by exactly one node");

        return Executors.execute(next, joinedMessage, graph);
    }

}