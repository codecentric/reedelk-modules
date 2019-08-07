package com.esb.component;

import com.esb.graph.ExecutionNode;
import com.esb.system.component.Fork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForkWrapper extends Fork {

    private List<ExecutionNode> forkNodes = new ArrayList<>();

    private ExecutionNode stopNode;

    public void addForkNode(ExecutionNode executionNode) {
        this.forkNodes.add(executionNode);
    }

    public ExecutionNode getStopNode() {
        return this.stopNode;
    }

    public void setStopNode(ExecutionNode stopNode) {
        this.stopNode = stopNode;
    }

    public List<ExecutionNode> getForkNodes() {
        return Collections.unmodifiableList(forkNodes);
    }
}
