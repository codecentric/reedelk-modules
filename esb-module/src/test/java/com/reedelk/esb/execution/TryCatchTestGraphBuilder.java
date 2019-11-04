package com.reedelk.esb.execution;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.component.TryCatchWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.component.Stop;

import static com.reedelk.esb.execution.AbstractExecutionTest.newExecutionNode;

class TryCatchTestGraphBuilder {

    private ExecutionNode tryNode;
    private ExecutionNode inbound;
    private ExecutionNode catchNode;
    private ExecutionNode tryCatchNode;

    private ComponentDisposer disposer;

    static TryCatchTestGraphBuilder get() {
        return new TryCatchTestGraphBuilder();
    }

    TryCatchTestGraphBuilder inbound(ExecutionNode inbound) {
        this.inbound = inbound;
        return this;
    }

    TryCatchTestGraphBuilder tryNode(ExecutionNode tryNode) {
        this.tryNode = tryNode;
        return this;
    }

    TryCatchTestGraphBuilder catchNode(ExecutionNode catchNode) {
        this.catchNode = catchNode;
        return this;
    }

    TryCatchTestGraphBuilder disposer(ComponentDisposer disposer) {
        this.disposer = disposer;
        return this;
    }

    TryCatchTestGraphBuilder tryCatchNode(ExecutionNode tryCatchNode) {
        this.tryCatchNode = tryCatchNode;
        return this;
    }

    ExecutionGraph build() {
        ExecutionGraph graph = ExecutionGraph.build();
        graph.putEdge(null, inbound);
        graph.putEdge(inbound, tryCatchNode);
        graph.putEdge(tryCatchNode, tryNode);
        graph.putEdge(tryCatchNode, catchNode);

        ExecutionNode endOfTryCatch = newExecutionNode(disposer, new Stop());
        TryCatchWrapper tryCatchWrapper = (TryCatchWrapper) tryCatchNode.getComponent();
        tryCatchWrapper.setStopNode(endOfTryCatch);
        tryCatchWrapper.setFirstTryNode(tryNode);
        tryCatchWrapper.setFirstCatchNode(catchNode);

        graph.putEdge(tryNode, endOfTryCatch);
        graph.putEdge(catchNode, endOfTryCatch);

        ExecutionNode endOfGraphNode = newExecutionNode(disposer, new Stop());
        graph.putEdge(endOfTryCatch, endOfGraphNode);
        return graph;
    }
}
