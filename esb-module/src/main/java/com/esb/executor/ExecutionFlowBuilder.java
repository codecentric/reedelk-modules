package com.esb.executor;

import com.esb.api.component.Component;
import com.esb.component.ForkWrapper;
import com.esb.component.RouterWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import com.esb.system.component.Stop;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionFlowBuilder {

    private static final ProcessorFlowBuilder DEFAULT_EXECUTOR = new ProcessorFlowBuilder();

    private static final Map<Class, FlowBuilder> COMPONENT_EXECUTOR;

    static {
        Map<Class, FlowBuilder> tmp = new HashMap<>();
        tmp.put(Stop.class, new StopFlowBuilder());
        tmp.put(ForkWrapper.class, new ForkFlowBuilder());
        tmp.put(RouterWrapper.class, new RouterFlowBuilder());
        COMPONENT_EXECUTOR = Collections.unmodifiableMap(tmp);
    }

    private ExecutionFlowBuilder() {
    }

    public static Flux<ReactiveMessageContext> build(ExecutionNode next, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {
        Component component = next.getComponent();
        FlowBuilder flowBuilder = COMPONENT_EXECUTOR.getOrDefault(component.getClass(), DEFAULT_EXECUTOR);
        return flowBuilder.build(next, graph, parentFlux);
    }

}
