package com.reedelk.esb.execution;

import com.reedelk.esb.component.ForkWrapper;
import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.component.Join;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.TypedContent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.reedelk.esb.execution.ExecutionUtils.nextNode;
import static com.reedelk.runtime.api.commons.Preconditions.checkNotNull;
import static com.reedelk.runtime.api.commons.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class ForkExecutor implements FlowExecutor {

    @Override
    public Publisher<MessageAndContext> execute(Publisher<MessageAndContext> publisher, ExecutionNode currentNode, ExecutionGraph graph) {

        ForkWrapper fork = (ForkWrapper) currentNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = fork.getForkNodes();

        ExecutionNode stopNode = fork.getStopNode();

        ExecutionNode joinNode = nextNode(stopNode, graph);
        checkNotNull(joinNode, "Join component is mandatory after Fork");

        Component joinComponent = joinNode.getComponent();
        checkState(joinComponent instanceof Join,
                format("Fork must be followed by a component implementing [%s] interface", Join.class.getName()));

        Join join = (Join) joinComponent;

        Flux<MessageAndContext> flux = Flux.from(publisher).flatMap(messageContext -> {

            // We must consume the message stream if it has not been consumed yet,
            // otherwise we cannot copy (by using serialization) its content and hand
            // it over to the Fork branches in the Message payload.
            TypedContent<?> content = messageContext.getMessage().content();
            if (!content.isConsumed()) {
                content.consume();
            }

            // Create fork branches (Fork step)
            List<Mono<MessageAndContext>> forkBranches = nextExecutionNodes.stream()
                    .map(nextExecutionNode -> createForkBranch(
                            nextExecutionNode,
                            messageContext,
                            graph,
                            flowScheduler()))
                    .collect(toList());

            // Join fork branches (Join step)
            return Mono.zip(forkBranches, messagesCombinator())
                    .flatMap(eventsToJoin -> Mono.create(new JoinConsumer(messageContext, eventsToJoin, join)))
                    .publishOn(flowScheduler()); // switch back using another flow thread.
        });


        // Continue to execute the flow after join
        ExecutionNode nodeAfterJoin = nextNode(joinNode, graph);

        return FlowExecutorFactory.get().execute(flux, nodeAfterJoin, graph);
    }

    Scheduler flowScheduler() {
        return SchedulerProvider.flow();
    }

    private Mono<MessageAndContext> createForkBranch(ExecutionNode executionNode, MessageAndContext context, ExecutionGraph graph, Scheduler forkScheduler) {
        MessageAndContext messageCopy = context.copy();
        Mono<MessageAndContext> parent = Mono.just(messageCopy).publishOn(forkScheduler);
        Publisher<MessageAndContext> forkBranchPublisher = FlowExecutorFactory.get().execute(parent, executionNode, graph);
        return Mono.from(forkBranchPublisher);
    }

    private Function<Object[], MessageAndContext[]> messagesCombinator() {
        return objects -> {
            MessageAndContext[] messageAndContexts = new MessageAndContext[objects.length];
            for (int i = 0; i < objects.length; i++) {
                messageAndContexts[i] = (MessageAndContext) objects[i];
            }
            return messageAndContexts;
        };
    }

    static class JoinConsumer implements Consumer<MonoSink<MessageAndContext>> {

        private final Join join;
        private final MessageAndContext context;
        private final MessageAndContext[] messages;

        JoinConsumer(MessageAndContext originalMessage, MessageAndContext[] messagesToJoin, Join join) {
            this.join = join;
            this.context = originalMessage;
            this.messages = messagesToJoin;
        }

        @Override
        public void accept(MonoSink<MessageAndContext> sink) {
            try {
                List<Message> collect = stream(messages)
                        .map(MessageAndContext::getMessage)
                        .collect(toList());

                Message outMessage = join.apply(collect, context.getFlowContext());

                context.replaceWith(outMessage);

                sink.success(context);

            } catch (Exception e) {
                sink.error(e);
            }
        }
    }
}


