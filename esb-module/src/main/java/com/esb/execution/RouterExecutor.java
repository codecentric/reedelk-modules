package com.esb.execution;

import com.esb.api.message.Message;
import com.esb.api.service.ScriptEngineService;
import com.esb.component.RouterWrapper;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import com.esb.services.scriptengine.ESBJavascriptEngine;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.script.ScriptException;
import java.util.List;

import static com.esb.execution.ExecutionUtils.nextNode;
import static java.util.stream.Collectors.toList;

public class RouterExecutor implements FlowExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RouterExecutor.class);

    private static final ScriptEngineService ENGINE = ESBJavascriptEngine.INSTANCE;

    private final Mono<Boolean> FALSE = Mono.just(false);

    @Override
    public Publisher<EventContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<EventContext> publisher) {

        RouterWrapper router = (RouterWrapper) executionNode.getComponent();

        List<RouterWrapper.PathExpressionPair> pathExpressionPairs = router.getPathExpressionPairs();

        // Need to keep going and continue to execute the flow after the choice joins...
        Flux<EventContext> flux = Flux.from(publisher).flatMap(messageContext -> {

            // Create choice branches
            List<Mono<EventContext>> choiceBranches = pathExpressionPairs.stream()
                    .map(pathExpressionPair -> createConditionalBranch(pathExpressionPair, messageContext, graph))
                    .collect(toList());

            // Create a flow with all conditional flows, only the flow
            // evaluating to true will be executed.
            return Flux.concat(choiceBranches)
                    .take(1) // We just select the first one, in case there are more than one matching
                    .switchIfEmpty(subscriber -> // If there is no match, the default path is then executed
                            createDefaultMono(router.getDefaultPathOrThrow(), messageContext, graph)
                                    .subscribe(subscriber));
        });

        ExecutionNode stopNode = router.getEndOfRouterStopNode();

        ExecutionNode nodeAfterStop = nextNode(stopNode, graph);

        return FlowExecutorFactory.get().build(nodeAfterStop, graph, flux);
    }

    private Mono<EventContext> createDefaultMono(RouterWrapper.PathExpressionPair pair, EventContext message, ExecutionGraph graph) {
        ExecutionNode defaultExecutionNode = pair.pathReference;
        Flux<EventContext> parent = Flux.just(message);
        return Mono.from(FlowExecutorFactory.get()
                .build(defaultExecutionNode, graph, parent));
    }

    private Mono<EventContext> createConditionalBranch(RouterWrapper.PathExpressionPair pair, EventContext message, ExecutionGraph graph) {
        String expression = pair.expression;
        ExecutionNode pathExecutionNode = pair.pathReference;
        // This Mono evaluates the expression. If the expression is true,
        // then the branch subflow gets executed, otherwise the message is dropped
        // and the flow is not executed.
        Mono<EventContext> parent = Mono.just(message)
                .filterWhen(value -> evaluate(expression, message.getMessage()));

        return Mono.from(FlowExecutorFactory.get().build(pathExecutionNode, graph, parent));
    }

    private Mono<Boolean> evaluate(String expression, Message message) {
        try {
            Boolean evaluate = ENGINE.evaluate(message, expression, boolean.class);
            return Mono.just(evaluate);
        } catch (ScriptException e) {
            logger.error(String.format("Could not evaluate Router path expression (%s)", expression), e);
            return FALSE;
        }
    }
}
