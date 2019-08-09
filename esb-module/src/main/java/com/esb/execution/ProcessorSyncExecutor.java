package com.esb.execution;

import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.esb.execution.ExecutionUtils.nextNode;
import static com.esb.execution.ExecutionUtils.nullSafeMap;
import static reactor.core.publisher.Mono.from;

public class ProcessorSyncExecutor implements FlowExecutor {

    @Override
    public Publisher<EventContext> execute(Publisher<EventContext> publisher, ExecutionNode currentNode, ExecutionGraph graph) {

        ProcessorSync processorSync = (ProcessorSync) currentNode.getComponent();

        Mono<EventContext> mono =
                from(publisher)
                        .handle(nullSafeMap(map(processorSync)));

        ExecutionNode next = nextNode(currentNode, graph);

        // Move on building the flux for the following
        // processors in the execution graph definition.
        return FlowExecutorFactory.get().execute(mono, next, graph);
    }

    private Function<EventContext, EventContext> map(ProcessorSync processor) {
        return event -> {
            // The context contains the input Flow Message.
            Message inMessage = event.getMessage();

            // Apply the input Message to the processor and we
            // let it process it (transform) to its new value.
            Message outMessage = processor.apply(inMessage);

            // We replace in the context the new output message.
            event.replaceWith(outMessage);

            return event;
        };
    }
}
