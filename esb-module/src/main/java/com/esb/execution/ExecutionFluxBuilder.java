package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.component.ForkWrapper;
import com.esb.component.RouterWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import com.esb.system.component.Stop;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionFluxBuilder {

    private static final GenericProcessorFluxBuilder DEFAULT_EXECUTOR = new GenericProcessorFluxBuilder();

    private static final Map<Class, FluxBuilder> COMPONENT_EXECUTOR;

    static {
        Map<Class, FluxBuilder> tmp = new HashMap<>();
        tmp.put(Stop.class, new StopFluxBuilder());
        tmp.put(ForkWrapper.class, new ForkFluxBuilder());
        tmp.put(RouterWrapper.class, new RouterFluxBuilder());
        COMPONENT_EXECUTOR = Collections.unmodifiableMap(tmp);
    }

    private ExecutionFluxBuilder() {
    }

    public static Flux<MessageContext> build(ExecutionNode next, ExecutionGraph graph, Flux<MessageContext> parent) {
        Component component = next.getComponent();
        FluxBuilder fluxBuilder = COMPONENT_EXECUTOR.getOrDefault(component.getClass(), DEFAULT_EXECUTOR);
        return fluxBuilder.build(next, graph, parent);
    }

    public static Mono<MessageContext> build(ExecutionNode next, ExecutionGraph graph, Mono<MessageContext> parent) {
        Component component = next.getComponent();
        FluxBuilder fluxBuilder = COMPONENT_EXECUTOR.getOrDefault(component.getClass(), DEFAULT_EXECUTOR);
        return fluxBuilder.build(next, graph, parent);
    }

}
