package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.api.service.ScriptEngineService;
import com.esb.component.RouterWrapper;
import com.esb.component.RouterWrapper.PathExpressionPair;
import com.esb.flow.ExecutionNode;
import com.esb.graph.ExecutionGraph;
import com.esb.services.scriptengine.ESBJavascriptEngine;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.script.ScriptException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;
import static java.util.stream.Collectors.toList;

public class RouterFlowBuilder implements FlowBuilder {

    private static final ScriptEngineService ENGINE = ESBJavascriptEngine.INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(RouterFlowBuilder.class);

    @Override
    public Flux<ReactiveMessageContext> build(ExecutionNode executionNode, ExecutionGraph graph, Flux<ReactiveMessageContext> parentFlux) {

        RouterWrapper router = (RouterWrapper) executionNode.getComponent();

        List<RouterWrapper.PathExpressionPair> pathExpressionPairs = router.getPathExpressionPairs();

        // Need to keep going and continue to build the flow after the choice joins...
        Flux<ReactiveMessageContext> newParent = parentFlux.flatMap((Function<ReactiveMessageContext, Publisher<ReactiveMessageContext>>) context -> {
            List<Mono<ReactiveMessageContext>> choiceFluxes = pathExpressionPairs.stream()
                    .map(pathExpressionPair -> createConditionalMonoFromExpressionPair(pathExpressionPair, context, graph))
                    .collect(toList());
            // Create a flow with all conditional flows, only the flow evaluating to true will be executed.
            return Flux.concat(choiceFluxes)
                    .take(1) // We just select the first one, in case there are more than one matching
                    // If there is no match, the default path is then executed
                    .switchIfEmpty(subscriber -> createDefaultMono(router.getDefaultPathOrThrow(), context, graph)
                            .subscribe(subscriber));
        });

        ExecutionNode endOfRouterStopNode = router.getEndOfRouterStopNode();
        Collection<ExecutionNode> successors = graph.successors(endOfRouterStopNode);


        ExecutionNode nodeAfterRouterStopNode = checkAtLeastOneAndGetOrThrow(
                successors.stream(),
                "End of router stop node must be followed by exactly one node");

        return ExecutionFlowBuilder.build(nodeAfterRouterStopNode, graph, newParent);
    }

    private Mono<ReactiveMessageContext> createDefaultMono(PathExpressionPair pair, ReactiveMessageContext message, ExecutionGraph graph) {
        ExecutionNode defaultExecutionNode = pair.pathReference;

        Mono<ReactiveMessageContext> parent = Mono.just(message);

        return ExecutionFlowBuilder.build(defaultExecutionNode, graph, parent);
    }

    private Mono<ReactiveMessageContext> createConditionalMonoFromExpressionPair(PathExpressionPair pair, ReactiveMessageContext message, ExecutionGraph graph) {
        String expression = pair.expression;
        ExecutionNode pathExecutionNode = pair.pathReference;

        Mono<ReactiveMessageContext> parent = Mono.just(message)
                .filterWhen(value -> evaluate(expression, message.getMessage()));

        return ExecutionFlowBuilder.build(pathExecutionNode, graph, parent);
    }

    private Mono<Boolean> evaluate(String expression, Message message) {
        try {
            Boolean evaluate = ENGINE.evaluate(message, expression, boolean.class);
            return Mono.just(evaluate);
        } catch (ScriptException e) {
            logger.error(String.format("Could not evaluate Router path expression (%s)", expression), e);
            return Mono.just(false);
        }
    }
}
