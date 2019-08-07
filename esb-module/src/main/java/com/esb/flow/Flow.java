package com.esb.flow;

import com.esb.api.component.Inbound;
import com.esb.api.component.InboundEventListener;
import com.esb.api.component.OnResult;
import com.esb.api.message.Message;
import com.esb.execution.FlowExecutor;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.esb.commons.Preconditions.checkArgument;
import static com.esb.commons.Preconditions.checkState;
import static java.lang.String.format;

public class Flow implements InboundEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    private final String flowId;
    private final FlowExecutor flowExecutor;
    private final ExecutionGraph executionGraph;

    private boolean started = false;

    public Flow(final String flowId, final ExecutionGraph executionGraph) {
        this.flowId = flowId;
        this.executionGraph = executionGraph;
        this.flowExecutor = new FlowExecutor(executionGraph);
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
            flowExecutor.onEvent(message, onResult);
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
