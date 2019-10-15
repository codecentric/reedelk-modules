package com.reedelk.esb.commons;

import com.reedelk.esb.flow.Flow;
import com.reedelk.esb.module.Module;
import com.reedelk.runtime.commons.JsonParser;
import org.json.JSONObject;
import org.slf4j.Logger;

import static com.reedelk.esb.commons.Messages.Flow.*;
import static com.reedelk.esb.commons.Messages.Module.DESERIALIZATION_ERROR;
import static com.reedelk.esb.commons.Messages.Module.VALIDATION_ERROR;

public class Log {

    // Module

    public static void deserializationException(Logger logger, Module module, Exception exception) {
        if (logger.isErrorEnabled()) {
            String message = DESERIALIZATION_ERROR.format(
                    module.id(),
                    module.name(),
                    module.version(),
                    module.filePath());
            logger.error(message, exception);
        }
    }

    public static void validationException(Logger logger, Module module, Exception exception) {
        if (logger.isErrorEnabled()) {
            String message = VALIDATION_ERROR.format(
                    module.id(),
                    module.name(),
                    module.version(),
                    module.filePath());
            logger.error(message, exception);
        }
    }

    // Flow JSON definition

    public static void buildException(Logger logger, JSONObject flowDefinition, String flowId, Exception exception) {
        if (logger.isErrorEnabled()) {
            String message;
            if (JsonParser.Flow.hasTitle(flowDefinition)) {
                String flowTitle = JsonParser.Flow.title(flowDefinition);
                message = BUILD_ERROR_WITH_TITLE.format(flowId, flowTitle);
            } else {
                message = BUILD_ERROR.format(flowId);
            }
            logger.error(message, exception);
        }
    }

    // Flow

    public static void flowStarted(Logger logger, Flow flow) {
        if (logger.isDebugEnabled()) {
            String message = flow.getFlowTitle()
                    .map(flowTitle -> START_WITH_TITLE.format(flow.getFlowId(), flowTitle))
                    .orElse(START.format(flow.getFlowId()));
            logger.debug(message);
        }
    }

    public static void flowForceStopException(Logger logger, Flow flow, Exception exception) {
        if (logger.isWarnEnabled()) {
            String message = flow.getFlowTitle()
                    .map(flowTitle -> FORCE_STOP_WITH_TITLE.format(flow.getFlowId(), flowTitle))
                    .orElse(FORCE_STOP.format(flow.getFlowId()));
            logger.warn(message, exception);
        }
    }

    public static void flowStartException(Logger logger, Flow flow, Exception exception) {
        if (logger.isErrorEnabled()) {
            String message = flow.getFlowTitle()
                    .map(flowTitle -> START_ERROR_WITH_TITLE.format(flow.getFlowId(), flowTitle))
                    .orElse(START_ERROR.format(flow.getFlowId()));
            logger.error(message, exception);
        }
    }

    public static void flowStopException(Logger logger, Flow flow, Exception exception) {
        if (logger.isErrorEnabled()) {
            String message = flow.getFlowTitle()
                    .map(flowTitle -> STOP_ERROR_WITH_TITLE.format(flow.getFlowId(), flowTitle))
                    .orElse(STOP_ERROR.format(flow.getFlowId()));
            logger.error(message, exception);
        }
    }
}
