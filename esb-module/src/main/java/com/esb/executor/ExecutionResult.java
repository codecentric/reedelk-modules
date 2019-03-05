package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.flow.ExecutionNode;

public class ExecutionResult {

    private final Message message;
    private final ExecutionNode lastExecutedNode;

    public ExecutionResult(final Message message, final ExecutionNode lastExecutedNode) {
        this.message = message;
        this.lastExecutedNode = lastExecutedNode;
    }

    public Message getMessage() {
        return message;
    }

    public ExecutionNode getLastExecutedNode() {
        return lastExecutedNode;
    }
}
