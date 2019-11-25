package com.reedelk.esb.flow;

import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.graph.ExecutionGraph;

public class ErrorStateFlow extends Flow {

    private final Exception exception;

    public ErrorStateFlow(long moduleId, String flowId, String flowTitle, ExecutionGraph executionGraph, FlowExecutorEngine executionEngine, Exception exception) {
        super(moduleId, flowId, flowTitle, executionGraph, executionEngine);
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }
}
