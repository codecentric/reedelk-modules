package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.commons.Preconditions;
import com.esb.component.ForkWrapper;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;
import static java.util.stream.Collectors.toList;

public class ForkFluxBuilder implements FluxBuilder {

    private Scheduler fluxExecutionScheduler = Schedulers.single();

    @Override
    public Flux<MessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<MessageContext> parentFlux) {

        ForkWrapper fork = (ForkWrapper) executionNode.getComponent();

        int threadPoolSize = fork.getThreadPoolSize();

        // THIS One should be returned by the fork wrapper so that
        // it can be disposed directly from within the component
        //TODO: This scheduler must be disposed then the flow is shutdown
        Scheduler forkScheduler = Schedulers.newParallel("ForkScheduler", threadPoolSize);

        List<ExecutionNode> nextExecutionNodes = fork.getForkNodes();

        ExecutionNode stopNode = fork.getStopNode();

        ExecutionNode joinExecutionNode = getNextNodeOrThrow(graph, stopNode,
                "Fork stop node must be followed by one node");

        Component joinComponent = joinExecutionNode.getComponent();
        Preconditions.checkState(joinComponent instanceof Join,
                String.format("Fork must be followed by a component implementing %s interface", Join.class.getName()));

        Join join = (Join) joinComponent;

        Flux<MessageContext> newParent =
                parentFlux
                        .subscribeOn(fluxExecutionScheduler)
                        .flatMap(context -> {
                            // Create fluxes running on scheduler (fork step)
                            List<Mono<MessageContext>> forkFluxes = nextExecutionNodes.stream()
                                    .map(forkExecutionNodePath ->
                                            createForkMonoFrom(forkExecutionNodePath, context, graph)
                                                    .subscribeOn(forkScheduler))
                                    .collect(toList());

                            // Join the fluxes (Join step)
                            return Flux.zip(forkFluxes, messagesCombinator())
                                    .flatMap(reactiveMessageContexts ->
                                            Mono.create(new JoinConsumer(context, reactiveMessageContexts, join))
                                                    .subscribeOn(fluxExecutionScheduler));
                        });

        // Continue to build the flow after join
        ExecutionNode nodeAfterJoin =
                getNextNodeOrThrow(graph, joinExecutionNode, "Join must be followed by one node");

        return ExecutionFluxBuilder.build(nodeAfterJoin, graph, newParent);

    }

    private static ExecutionNode getNextNodeOrThrow(ExecutionGraph graph, ExecutionNode node, String message) {
        Collection<ExecutionNode> successors = graph.successors(node);
        return checkAtLeastOneAndGetOrThrow(successors.stream(), message);
    }

    private Mono<MessageContext> createForkMonoFrom(ExecutionNode executionNode, MessageContext context, ExecutionGraph graph) {
        MessageContext messageCopy = context.copy();
        Mono<MessageContext> parent = Mono.just(messageCopy);
        return ExecutionFluxBuilder
                .build(executionNode, graph, parent);
    }

    private static Function<Object[], MessageContext[]> messagesCombinator() {
        return objects -> {
            MessageContext[] messageContexts = new MessageContext[objects.length];
            for (int i = 0; i < objects.length; i++) {
                messageContexts[i] = (MessageContext) objects[i];
            }
            return messageContexts;
        };
    }

    class JoinConsumer implements Consumer<MonoSink<MessageContext>> {

        private final Join join;
        private final MessageContext context;
        private final MessageContext[] messages;

        JoinConsumer(MessageContext originalMessage, MessageContext[] messagesToJoin, Join join) {
            this.join = join;
            this.context = originalMessage;
            this.messages = messagesToJoin;
        }

        @Override
        public void accept(MonoSink<MessageContext> sink) {
            try {
                List<Message> collect = Arrays
                        .stream(messages)
                        .map(MessageContext::getMessage)
                        .collect(toList());
                Message outMessage = join.apply(collect);
                context.replace(outMessage);
                sink.success(context);
            } catch (Exception e) {
                context.onError(e);
                sink.success();
            }
        }
    }
}


