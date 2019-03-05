package com.esb.executor;

import com.esb.api.component.Component;
import com.esb.api.message.Message;
import com.esb.commons.Graph;
import com.esb.component.Choice;
import com.esb.component.Fork;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Executors {

    private static final ProcessorExecutor DEFAULT_EXECUTOR = new ProcessorExecutor();

    private static final Map<Class, Executor> COMPONENT_EXECUTOR = ImmutableMap.of(
            Stop.class, new StopExecutor(),
            Fork.class, new ForkExecutor(),
            Choice.class, new ChoiceExecutor());

    private Executors() {
    }

    public static ExecutionResult execute(ExecutionNode next, Message message, Graph graph) {
        Component component = next.getComponent();
        Executor executor = COMPONENT_EXECUTOR.getOrDefault(component.getClass(), DEFAULT_EXECUTOR);

        return executor.execute(next, message, graph);
    }

}
