package com.reedelk.esb.flow;

import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.graph.ExecutionGraph;

public class ErrorStateFlow extends Flow {

    private final Exception exception;

    public ErrorStateFlow(String flowId, String flowTitle, ExecutionGraph executionGraph, FlowExecutorEngine executionEngine, Exception exception) {
        super(flowId, flowTitle, executionGraph, executionEngine);
        this.exception = exception;
    }

    public ErrorStateFlow(ExecutionGraph executionGraph, FlowExecutorEngine executionEngine, Exception exception) {
        super(null, null, executionGraph, executionEngine);
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }

}
