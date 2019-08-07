package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.ProcessorAsync;
import com.esb.api.component.ProcessorSync;
import com.esb.component.ForkWrapper;
import com.esb.component.RouterWrapper;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import com.esb.system.component.Stop;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionFluxBuilder {

    private static final ExecutionFluxBuilder INSTANCE = new ExecutionFluxBuilder();

    private static final Map<Class, FluxBuilder> COMPONENT_FLUX_BUILDER;
    static {
        Map<Class, FluxBuilder> tmp = new HashMap<>();
        tmp.put(Stop.class, new StopFluxBuilder());
        tmp.put(ForkWrapper.class, new ForkFluxBuilder());
        tmp.put(RouterWrapper.class, new RouterFluxBuilder());
        tmp.put(ProcessorSync.class, new GenericProcessorSyncFluxBuilder());
        tmp.put(ProcessorAsync.class, new GenericProcessorAsyncFluxBuilder());
        COMPONENT_FLUX_BUILDER = Collections.unmodifiableMap(tmp);
    }

    private ExecutionFluxBuilder() {
    }

    public static ExecutionFluxBuilder get() {
        return INSTANCE;
    }

    public Flux<MessageContext> build(ExecutionNode next, ExecutionGraph graph, Flux<MessageContext> parent) {
        FluxBuilder builder = getBuilderOrThrow(next);
        return builder.build(next, graph, parent);
    }

    public Mono<MessageContext> build(ExecutionNode next, ExecutionGraph graph, Mono<MessageContext> parent) {
        FluxBuilder builder = getBuilderOrThrow(next);
        return builder.build(next, graph, parent);
    }

    private FluxBuilder getBuilderOrThrow(ExecutionNode executionNode) {
        Component component = executionNode.getComponent();
        if (COMPONENT_FLUX_BUILDER.containsKey(component.getClass())) {
            return COMPONENT_FLUX_BUILDER.get(component.getClass());
        }
        throw new IllegalStateException(String.format("Could not find flux builder for class [%s]", component.getClass()));
    }
}
