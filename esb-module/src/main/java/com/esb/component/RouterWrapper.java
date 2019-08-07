package com.esb.component;

import com.esb.api.exception.ESBException;
import com.esb.graph.ExecutionNode;
import com.esb.system.component.Router;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class RouterWrapper extends Router {

    private List<PathExpressionPair> pathExpressionPairs = new ArrayList<>();
    private ExecutionNode endOfRouterStopNode;

    /**
     * Returns all paths excluding the default.
     */
    public List<PathExpressionPair> getPathExpressionPairs() {
        return pathExpressionPairs.stream()
                .filter(pathExpressionPair -> !pathExpressionPair.expression.equals(DEFAULT_CONDITION))
                .collect(toList());
    }

    public void addPathExpressionPair(String expression, ExecutionNode pathExecutionNode) {
        pathExpressionPairs.add(new PathExpressionPair(expression, pathExecutionNode));
    }

    public PathExpressionPair getDefaultPathOrThrow() {
        return pathExpressionPairs.stream()
                .filter(pathExpressionPair -> pathExpressionPair.expression.equals(DEFAULT_CONDITION))
                .findFirst()
                .orElseThrow(() -> new ESBException("Default router condition could not be found"));
    }

    public void setEndOfRouterStopNode(ExecutionNode endOfRouterStopNode) {
        this.endOfRouterStopNode = endOfRouterStopNode;
    }

    public ExecutionNode getEndOfRouterStopNode() {
        return endOfRouterStopNode;
    }

    public class PathExpressionPair {
        public final String expression;
        public final ExecutionNode pathReference;

        PathExpressionPair(String expression, ExecutionNode pathReference) {
            this.expression = expression;
            this.pathReference = pathReference;
        }
    }
}
