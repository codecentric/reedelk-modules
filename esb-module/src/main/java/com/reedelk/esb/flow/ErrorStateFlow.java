package com.reedelk.esb.flow;

import com.reedelk.esb.graph.ExecutionGraph;

public class ErrorStateFlow extends Flow {

    private final Exception exception;

    public ErrorStateFlow(String flowId, ExecutionGraph executionGraph, Exception exception) {
        super(flowId, executionGraph);
        this.exception = exception;
    }

    public ErrorStateFlow(ExecutionGraph executionGraph, Exception exception) {
        super(null, executionGraph);
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }

}
