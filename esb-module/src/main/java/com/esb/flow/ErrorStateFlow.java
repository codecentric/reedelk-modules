package com.esb.flow;

import com.esb.commons.ESBExecutionGraph;

public class ErrorStateFlow extends Flow {

    private final Exception exception;

    public ErrorStateFlow(String flowId, ESBExecutionGraph executionGraph, Exception exception) {
        super(flowId, executionGraph);
        this.exception = exception;
    }

    public ErrorStateFlow(ESBExecutionGraph executionGraph, Exception exception) {
        super(null, executionGraph);
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }

}
