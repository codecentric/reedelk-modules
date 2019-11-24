package com.reedelk.esb.exception;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.ScriptBlockContext;

import static com.reedelk.esb.commons.Messages.Script.SCRIPT_EXECUTION_ERROR;
import static com.reedelk.runtime.api.commons.StackTraceUtils.rootCauseMessageOf;

public class ScriptExecutionException extends ESBException {

    public ScriptExecutionException(ScriptBlock scriptBlock, Throwable cause) {
        super(messageFrom(scriptBlock, cause), cause);
    }

    private static String messageFrom(ScriptBlock scriptBlock, Throwable cause) {
        ScriptBlockContext context = scriptBlock.context();
        Long moduleId = context.getModuleId();
        String flowTitle = context.getFlowTitle();
        String flowId = context.getFlowId();
        String body = scriptBlock.body();
        String error = rootCauseMessageOf(cause);
        return SCRIPT_EXECUTION_ERROR.format(
                error,
                error,
                moduleId,
                flowId,
                flowTitle,
                body);
    }
}
