package com.reedelk.esb.component;

import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicBoolean;
import com.reedelk.runtime.component.Router;

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
                .filter(pathExpressionPair -> !DEFAULT_CONDITION.equals(pathExpressionPair.expression))
                .collect(toList());
    }

    public void addExpressionAndPathPair(DynamicBoolean expression, ExecutionNode pathExecutionNode) {
        pathExpressionPairs.add(new PathExpressionPair(expression, pathExecutionNode));
    }

    public PathExpressionPair getDefaultPathOrThrow() {
        return pathExpressionPairs.stream()
                .filter(pathExpressionPair -> DEFAULT_CONDITION.equals(pathExpressionPair.expression))
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
        public final DynamicBoolean expression;
        public final ExecutionNode pathReference;

        PathExpressionPair(DynamicBoolean expression, ExecutionNode pathReference) {
            this.expression = expression;
            this.pathReference = pathReference;
        }
    }
}
