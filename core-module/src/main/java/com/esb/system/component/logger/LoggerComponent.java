package com.esb.system.component.logger;

import com.esb.api.annotation.*;
import com.esb.api.component.ProcessorSync;
import com.esb.api.exception.ESBException;
import com.esb.api.message.Message;
import com.esb.api.service.ScriptEngineService;
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

    @Required
    @Default("INFO")
    @Property("Logger Level")
    private LoggerLevel level;

    @Script
    @Default("message")
    @Property("Log message")
    private String message;

    @Override
    public Message apply(Message input) {
        try {
            Object result = service.evaluate(input, message, Object.class);
            level.log(result);
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
        return input;
    }

    public void setLevel(LoggerLevel level) {
        this.level = level;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
