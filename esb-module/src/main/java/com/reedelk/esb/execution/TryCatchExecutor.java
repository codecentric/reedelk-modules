package com.reedelk.esb.execution;

import com.reedelk.esb.component.TryCatchWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.ObjectContent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.reedelk.esb.execution.ExecutionUtils.nextNode;

public class TryCatchExecutor implements FlowExecutor {

    @Override
    public Publisher<MessageAndContext> execute(Publisher<MessageAndContext> publisher, ExecutionNode currentNode, ExecutionGraph graph) {

        TryCatchWrapper tryCatch = (TryCatchWrapper) currentNode.getComponent();

        ExecutionNode stopNode = tryCatch.getStopNode();

        ExecutionNode nodeAfterStop = nextNode(stopNode, graph);

        ExecutionNode firstTryNode = tryCatch.getFirstTryNode();

        ExecutionNode firstCatchNode = tryCatch.getFirstCatchNode();

        Flux<MessageAndContext> result = Flux.from(publisher).flatMap((Function<MessageAndContext, Mono<MessageAndContext>>) messageAndContext -> {

            Publisher<MessageAndContext> tryExecution =
                    FlowExecutorFactory.get().execute(Mono.just(messageAndContext), firstTryNode, graph);

            return Mono.from(tryExecution).onErrorResume(throwable -> {

                Mono<MessageAndContext> mapped = Mono.just(messageAndContext).map(context -> {

                    ObjectContent content = new ObjectContent(throwable, MimeType.APPLICATION_JAVA);

                    Message messageWithException = MessageBuilder.get().typedContent(content).build();

                    context.replaceWith(messageWithException);

                    return context;

                });

                return Mono.from(FlowExecutorFactory.get().execute(mapped, firstCatchNode, graph));

            });

        });

        return FlowExecutorFactory.get().execute(result, nodeAfterStop, graph);
    }
}
