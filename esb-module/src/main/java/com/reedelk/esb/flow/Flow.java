package com.reedelk.esb.flow;

import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Inbound;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.reedelk.esb.commons.Preconditions.checkArgument;
import static com.reedelk.esb.commons.Preconditions.checkState;
import static java.lang.String.format;

public class Flow implements InboundEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    private final String flowId;
    private final ExecutionGraph executionGraph;
    private final FlowExecutorEngine flowExecutorEngine;

    private boolean started = false;

    public Flow(final String flowId, final ExecutionGraph executionGraph) {
        this.flowId = flowId;
        this.executionGraph = executionGraph;
        this.flowExecutorEngine = new FlowExecutorEngine(executionGraph);
    }

    public String getFlowId() {
        return flowId;
    }

    public boolean isUsingComponent(String targetComponentName) {
        checkArgument(targetComponentName != null, "Component Name");

        Optional<ExecutionNode> found = executionGraph
                .findOne(executionNode -> executionNode.isUsingComponent(targetComponentName));
        return found.isPresent();
    }

    public void releaseReferences(Bundle bundle) {
        checkState(!isStarted(), "Flow references can be released only when the flow is stopped!");
        executionGraph.applyOnNodes(ReleaseReferenceConsumer.get(bundle));
    }

    public boolean isStarted() {
        synchronized (this) {
            return started;
        }
    }

    public void start() {
        synchronized (this) {
            Inbound inbound = getInbound();
            inbound.addEventListener(this);
            inbound.onStart();
            started = true;
        }
    }

    public void stopIfStarted() {
        synchronized (this) {
            if (started) {
                forceStop();
            }
        }
    }

    public void forceStop() {
        synchronized (this) {
            try {
                Inbound inbound = getInbound();
                inbound.removeEventListener();
                inbound.onShutdown();
            } finally {
                started = false;
            }
        }
    }

    @Override
    public void onEvent(Message message, OnResult onResult) {
        try {
            flowExecutorEngine.onEvent(message, onResult);
        } catch (Exception exception) {
            String errorMessage = format("Exception while executing Flow with id=[%s]", flowId);
            logger.debug(errorMessage, exception);
            throw exception;
        }
    }

    private Inbound getInbound() {
        return (Inbound) executionGraph.getRoot().getComponent();
    }
}
