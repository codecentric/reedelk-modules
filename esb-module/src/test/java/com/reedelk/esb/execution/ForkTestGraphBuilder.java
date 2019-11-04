package com.reedelk.esb.execution;

import com.reedelk.esb.commons.ComponentDisposer;
import com.reedelk.esb.component.ForkWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.component.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.reedelk.esb.execution.AbstractExecutionTest.newExecutionNode;

class ForkTestGraphBuilder extends AbstractTestGraphBuilder {

    private ExecutionNode fork;
    private ExecutionNode join;
    private ExecutionNode inbound;
    private ComponentDisposer disposer;
    private List<ForkSequence> forkSequenceList = new ArrayList<>();
    private List<ExecutionNode> followingSequence = new ArrayList<>();

    static ForkTestGraphBuilder get() {
        return new ForkTestGraphBuilder();
    }

    ForkTestGraphBuilder fork(ExecutionNode fork) {
        this.fork = fork;
        return this;
    }

    ForkTestGraphBuilder join(ExecutionNode join) {
        this.join = join;
        return this;
    }

    ForkTestGraphBuilder inbound(ExecutionNode inbound) {
        this.inbound = inbound;
        return this;
    }

    ForkTestGraphBuilder disposer(ComponentDisposer disposer) {
        this.disposer = disposer;
        return this;
    }

    ForkTestGraphBuilder forkSequence(ExecutionNode... sequence) {
        this.forkSequenceList.add(new ForkSequence(sequence));
        return this;
    }

    ForkTestGraphBuilder afterForkSequence(ExecutionNode... afterForkSequence) {
        this.followingSequence = Arrays.asList(afterForkSequence);
        return this;
    }

    ExecutionGraph build() {
        ExecutionGraph graph = ExecutionGraph.build();
        graph.putEdge(null, inbound);
        graph.putEdge(inbound, fork);

        ExecutionNode endOfFork = newExecutionNode(disposer, new Stop());

        ForkWrapper forkWrapper = (ForkWrapper) fork.getComponent();
        forkWrapper.setStopNode(endOfFork);
        for (ForkSequence sequence : forkSequenceList) {
            buildSequence(graph, fork, endOfFork, sequence.sequence);
            if (sequence.sequence.size() > 0) {
                forkWrapper.addForkNode(sequence.sequence.get(0));
            }
        }

        ExecutionNode last = endOfFork;

        if (join != null) {
            graph.putEdge(endOfFork, join);
            last = join;
        }

        ExecutionNode endOfGraph = newExecutionNode(disposer, new Stop());
        if (followingSequence.size() > 0) {
            buildSequence(graph, last, endOfGraph, followingSequence);
        } else {
            graph.putEdge(last, endOfGraph);
        }

        return graph;
    }

    class ForkSequence {
        List<ExecutionNode> sequence;

        ForkSequence(ExecutionNode[] sequence) {
            this.sequence = Arrays.asList(sequence);
        }
    }
}
