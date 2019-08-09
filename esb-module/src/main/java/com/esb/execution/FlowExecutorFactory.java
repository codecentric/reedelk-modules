package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.ProcessorAsync;
import com.esb.api.component.ProcessorSync;
import com.esb.component.ForkWrapper;
import com.esb.component.RouterWrapper;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import com.esb.system.component.Stop;
import org.reactivestreams.Publisher;

import java.util.*;

import static java.lang.String.format;

public class FlowExecutorFactory {

    private static final FlowExecutorFactory INSTANCE = new FlowExecutorFactory();

    private static final Map<Class, FlowExecutor> COMPONENT_FLUX_BUILDER;
    static {
        Map<Class, FlowExecutor> tmp = new HashMap<>();
        tmp.put(Stop.class, new StopExecutor());
        tmp.put(ForkWrapper.class, new ForkExecutor());
        tmp.put(RouterWrapper.class, new RouterExecutor());
        tmp.put(ProcessorSync.class, new ProcessorSyncExecutor());
        tmp.put(ProcessorAsync.class, new ProcessorAsyncExecutor());
        COMPONENT_FLUX_BUILDER = Collections.unmodifiableMap(tmp);
    }

    private FlowExecutorFactory() {
    }

    public static FlowExecutorFactory get() {
        return INSTANCE;
    }

    public Publisher<EventContext> build(ExecutionNode next, ExecutionGraph graph, Publisher<EventContext> parent) {
        return getComponentBuilderOrThrow(next.getComponent())
                .execute(next, graph, parent);
    }

    FlowExecutor getComponentBuilderOrThrow(Component component) {
        if (COMPONENT_FLUX_BUILDER.containsKey(component.getClass())) {
            return COMPONENT_FLUX_BUILDER.get(component.getClass());
        }
        // We check if any of the superclasses implement a known
        // type for which a builder has been defined.
        Class<?>[] componentInterfaces = component.getClass().getInterfaces();
        return getComponentFluxBuilder(componentInterfaces)
                .orElseThrow(() ->
                        new IllegalStateException(format("Could not find flux builder for class [%s]", component.getClass())));
    }

    private Optional<FlowExecutor> getComponentFluxBuilder(Class<?>[] componentInterfaces) {
        Set<Class> fluxBuilderInterfaceNames = COMPONENT_FLUX_BUILDER.keySet();
        for (Class interfaceClazz : componentInterfaces) {
            if (fluxBuilderInterfaceNames.contains(interfaceClazz)) {
                return Optional.of(COMPONENT_FLUX_BUILDER.get(interfaceClazz));
            }
        }
        return Optional.empty();
    }
}
