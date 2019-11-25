package com.reedelk.esb.flow;

import com.reedelk.esb.exception.FlowExecutionException;
import com.reedelk.esb.execution.FlowExecutorEngine;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Inbound;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Flow.EXECUTION_ERROR;
import static com.reedelk.esb.commons.Preconditions.checkArgument;
import static com.reedelk.esb.commons.Preconditions.checkState;

public class Flow implements InboundEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Flow.class);

    private final long moduleId;
    private final String flowId;
    private final String flowTitle;
    private final ExecutionGraph executionGraph;
    private final FlowExecutorEngine executionEngine;

    private boolean started = false;

    public Flow(final long moduleId, final String flowId, final String flowTitle, final ExecutionGraph executionGraph, final FlowExecutorEngine executionEngine) {
        this.moduleId = moduleId;
        this.flowId = flowId;
        this.flowTitle = flowTitle;
        this.executionGraph = executionGraph;
        this.executionEngine = executionEngine;
    }

    public String getFlowId() {
        return flowId;
    }

    public Optional<String> getFlowTitle() {
        return Optional.ofNullable(flowTitle);
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
            executionEngine.onEvent(message, new OnResult() {

                @Override
                public void onResult(Message message, FlowContext flowContext) {
                    onResult.onResult(message, flowContext);
                }

                @Override
                public void onError(Throwable throwable, FlowContext flowContext) {
                    String error = String.format("Error occurred for module id %d, flow id %s, flow title %s. %s",
                            moduleId, flowId, flowTitle, throwable.getMessage());
                    FlowExecutionException wrapped = new FlowExecutionException(error, throwable);
                    onResult.onError(wrapped,flowContext);
                }
            });

        } catch (Exception exception) {
            String error = EXECUTION_ERROR.format(flowId, exception.getMessage());
            logger.debug(error, exception);
            throw new ESBException(error, exception);
        }
    }

    private Inbound getInbound() {
        return (Inbound) executionGraph.getRoot().getComponent();
    }
}
