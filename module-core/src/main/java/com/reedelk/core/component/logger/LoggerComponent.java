package com.reedelk.core.component.logger;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicObject;
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

    @Default("#[message]")
    @Hint("my log message")
    @Property("Log message")
    private DynamicObject message;

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

    public void setLevel(LoggerLevel level) {
        this.level = level;
    }

    public void setMessage(DynamicObject message) {
        this.message = message;
    }

    private void debug(Message message, FlowContext flowContext) throws ScriptException {
        // The logger should just print the Stream object if it is a stream, otherwise if
        // the stream was resolved (hence loaded into memory) it should print the value.
        Object evaluationResult = service.evaluate(this.message, flowContext, message).orElse(null);
        level.log(evaluationResult);
    }
}
