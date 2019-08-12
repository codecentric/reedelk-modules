package com.reedelk.esb.component;

import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.component.Fork;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForkWrapper extends Fork {

    private List<ExecutionNode> forkNodes = new ArrayList<>();

    private volatile Scheduler scheduler;

    private ExecutionNode stopNode;

    public ExecutionNode getStopNode() {
        return this.stopNode;
    }

    public void setStopNode(ExecutionNode stopNode) {
        this.stopNode = stopNode;
    }

    public void addForkNode(ExecutionNode executionNode) {
        this.forkNodes.add(executionNode);
    }

    public List<ExecutionNode> getForkNodes() {
        return Collections.unmodifiableList(forkNodes);
    }

    public Scheduler getScheduler() {
        if (scheduler == null) {
            synchronized (this) {
                if (scheduler == null) {
                    scheduler = SchedulerProvider.fork(getThreadPoolSize());
                }
            }
        }
        return scheduler;
    }

    @Override
    public void dispose() {
        if (scheduler != null) {
            if (!scheduler.isDisposed()) {
                scheduler.dispose();
            }
        }
    }
}
