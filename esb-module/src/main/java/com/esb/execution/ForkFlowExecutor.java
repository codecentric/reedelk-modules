package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.commons.Preconditions;
import com.esb.component.ForkWrapper;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.esb.execution.ExecutionUtils.nextNodeOrThrow;
import static java.util.stream.Collectors.toList;
import static reactor.core.publisher.Mono.*;

public class ForkFlowExecutor implements FlowExecutor {

    @Override
    public Publisher<MessageContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<MessageContext> publisher) {

        // TODO: Fork pool property needs to be hooked up in the framework
        ForkWrapper fork = (ForkWrapper) executionNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = fork.getForkNodes();

        ExecutionNode stopNode = fork.getStopNode();

        ExecutionNode joinNode = nextNodeOrThrow(stopNode, graph);

        Component joinComponent = joinNode.getComponent();
        Preconditions.checkState(joinComponent instanceof Join,
                String.format("Fork must be followed by a component implementing %s interface", Join.class.getName()));

        Join join = (Join) joinComponent;

        Mono<MessageContext> mono = from(publisher).flatMap(messageContext -> {

            // Create fork branches (Fork step)
            List<Mono<MessageContext>> forkBranches = nextExecutionNodes.stream()
                    .map(nextExecutionNode -> createForkBranch(nextExecutionNode, messageContext, graph))
                    .collect(toList());

            // Join fork branches (Join step)
            return zip(forkBranches, messagesCombinator())
                    .flatMap(reactiveMessageContexts ->
                            create(new JoinConsumer(messageContext, reactiveMessageContexts, join))
                                    .publishOn(Schedulers.elastic()));
        });


        // Continue to execute the flow after join
        ExecutionNode nodeAfterJoin = nextNodeOrThrow(joinNode, graph);

        return FlowExecutorFactory.get().build(nodeAfterJoin, graph, mono);
    }

    private Mono<MessageContext> createForkBranch(ExecutionNode executionNode, MessageContext context, ExecutionGraph graph) {
        MessageContext messageCopy = context.copy();
        Mono<MessageContext> parent = Mono.just(messageCopy).publishOn(Schedulers.parallel());
        return Mono.from(FlowExecutorFactory.get().build(executionNode, graph, parent));
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
                context.replaceWith(outMessage);
                sink.success(context);
            } catch (Exception e) {
                context.onError(e);
                sink.success();
            }
        }
    }
}


