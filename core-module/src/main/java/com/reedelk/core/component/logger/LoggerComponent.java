package com.reedelk.core.component.logger;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Logger")
@Component(service = LoggerComponent.class, scope = PROTOTYPE)
public class LoggerComponent implements ProcessorSync {

    static final Logger logger = LoggerFactory.getLogger(LoggerComponent.class);

    @Reference
    private ScriptEngineService service;

    @Property("Logger Level")
    @Default("INFO")
    private LoggerLevel level;

    @ScriptInline
    @Default("#[payload]")
    @Hint("my log message")
    @Property("Log message")
    private String message;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        try {
            if (LoggerLevel.DEBUG.equals(level)) {
                // When level is DEBUG, we only debug if the debug is enabled.
                if (logger.isDebugEnabled()) {
                    debug(message, flowContext);
                }
            } else {
                debug(message, flowContext);
            }
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
        return message;
    }

    private void debug(Message input, FlowContext flowContext) throws ScriptException {
        if (ScriptUtils.isScript(message)) {
            // The logger should just print the Stream object if it is a stream, otherwise if
            // the stream was resolved (hence loaded into memory) it should print the value.
            Object result = service.evaluate(message, input, flowContext);
            level.log(result);
        } else {
            // If it is not a script we don't evaluate the message.
            level.log(message);
        }
    }

    public void setLevel(LoggerLevel level) {
        this.level = level;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
