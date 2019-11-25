package com.reedelk.esb.exception;

import com.reedelk.runtime.api.exception.ESBException;

import static com.reedelk.esb.commons.Messages.Flow.EXECUTION_ERROR;

public class FlowExecutionException extends ESBException {

    private final long moduleId;
    private final String moduleName;
    private final String flowId;
    private final String flowTitle;

    public FlowExecutionException(long moduleId,
                                  String moduleName,
                                  String flowId,
                                  String flowTitle,
                                  Throwable exception) {
        super(formatMessage(moduleId, moduleName, flowId, flowTitle, exception), exception);
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.flowId = flowId;
        this.flowTitle = flowTitle;
    }

    public long getModuleId() {
        return moduleId;
    }

    public String getFlowId() {
        return flowId;
    }

    public String getFlowTitle() {
        return flowTitle;
    }

    public String getModuleName() {
        return moduleName;
    }

    private static String formatMessage(long moduleId, String moduleName, String flowId, String flowTitle, Throwable throwable) {
        return EXECUTION_ERROR.format(
                moduleId,
                moduleName,
                flowId,
                flowTitle,
                throwable.getClass().getName(),
                throwable.getMessage());
    }
}
