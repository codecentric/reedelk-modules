package com.esb.executor;

import com.esb.api.component.Component;
import com.esb.api.message.Message;
import com.esb.commons.ExecutionGraph;
import com.esb.component.Choice;
import com.esb.component.Fork;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Executors {

    private static final ProcessorExecutor DEFAULT_EXECUTOR = new ProcessorExecutor();

    private static final Map<Class, Executor> COMPONENT_EXECUTOR;

    static {
        Map<Class, Executor> tmp = new HashMap<>();
        tmp.put(Stop.class, new StopExecutor());
        tmp.put(Fork.class, new ForkExecutor());
        tmp.put(Choice.class, new ChoiceExecutor());
        COMPONENT_EXECUTOR = Collections.unmodifiableMap(tmp);
    }

    private Executors() {
    }

    public static ExecutionResult execute(ExecutionNode next, Message message, ExecutionGraph graph) {
        Component component = next.getComponent();
        Executor executor = COMPONENT_EXECUTOR.getOrDefault(component.getClass(), DEFAULT_EXECUTOR);
        return executor.execute(next, message, graph);
    }

}
