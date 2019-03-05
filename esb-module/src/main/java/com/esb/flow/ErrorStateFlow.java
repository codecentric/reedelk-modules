package com.esb.flow;

import com.esb.commons.Graph;

public class ErrorStateFlow extends Flow {

    private final Exception exception;

    public ErrorStateFlow(String flowId, Graph executionGraph, Exception exception) {
        super(flowId, executionGraph);
        this.exception = exception;
    }

    public ErrorStateFlow(Graph executionGraph, Exception exception) {
        super(null, executionGraph);
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }

}
