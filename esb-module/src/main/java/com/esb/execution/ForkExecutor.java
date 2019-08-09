package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.component.ForkWrapper;
import com.esb.concurrency.SchedulerProvider;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkState;
import static com.esb.execution.ExecutionUtils.nextNode;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static reactor.core.publisher.Mono.*;

public class ForkExecutor implements FlowExecutor {

    @Override
    public Publisher<EventContext> execute(Publisher<EventContext> publisher, ExecutionNode currentNode, ExecutionGraph graph) {

        ForkWrapper fork = (ForkWrapper) currentNode.getComponent();

        Scheduler forkScheduler = fork.getScheduler();

        List<ExecutionNode> nextExecutionNodes = fork.getForkNodes();

        ExecutionNode stopNode = fork.getStopNode();

        ExecutionNode joinNode = nextNode(stopNode, graph);

        Component joinComponent = joinNode.getComponent();
        checkState(joinComponent instanceof Join,
                String.format("Fork must be followed by a component implementing %s interface", Join.class.getName()));

        Join join = (Join) joinComponent;

        Mono<EventContext> mono = from(publisher).flatMap(messageContext -> {

            // Create fork branches (Fork step)
            List<Mono<EventContext>> forkBranches = nextExecutionNodes.stream()
                    .map(nextExecutionNode ->
                            createForkBranch(nextExecutionNode, messageContext, graph, forkScheduler))
                    .collect(toList());

            // Join fork branches (Join step)
            return zip(forkBranches, messagesCombinator())
                    .flatMap(eventsToJoin -> create(new JoinConsumer(messageContext, eventsToJoin, join)))
                    .publishOn(SchedulerProvider.flow()); // back using the flow Threads
        });


        // Continue to execute the flow after join
        ExecutionNode nodeAfterJoin = nextNode(joinNode, graph);

        return FlowExecutorFactory.get().execute(mono, nodeAfterJoin, graph);
    }

    private Mono<EventContext> createForkBranch(ExecutionNode executionNode, EventContext context, ExecutionGraph graph, Scheduler forkScheduler) {
        EventContext messageCopy = context.copy();
        Mono<EventContext> parent =
                Mono.just(messageCopy)
                        .publishOn(forkScheduler);
        return Mono.from(FlowExecutorFactory.get().execute(parent, executionNode, graph));
    }

    private static Function<Object[], EventContext[]> messagesCombinator() {
        return objects -> {
            EventContext[] eventContexts = new EventContext[objects.length];
            for (int i = 0; i < objects.length; i++) {
                eventContexts[i] = (EventContext) objects[i];
            }
            return eventContexts;
        };
    }

    class JoinConsumer implements Consumer<MonoSink<EventContext>> {

        private final Join join;
        private final EventContext context;
        private final EventContext[] messages;

        JoinConsumer(EventContext originalMessage, EventContext[] messagesToJoin, Join join) {
            this.join = join;
            this.context = originalMessage;
            this.messages = messagesToJoin;
        }

        @Override
        public void accept(MonoSink<EventContext> sink) {
            try {
                List<Message> collect = stream(messages)
                        .map(EventContext::getMessage)
                        .collect(toList());

                Message outMessage = join.apply(collect);

                context.replaceWith(outMessage);

                sink.success(context);

            } catch (Exception e) {
                sink.error(e);
            }
        }
    }
}


