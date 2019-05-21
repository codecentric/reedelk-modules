package com.esb.component.logger;

import com.esb.api.annotation.Default;
import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Required;
import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.api.message.TypedContent;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Logger")
@Component(service = LoggerComponent.class, scope = PROTOTYPE)
public class LoggerComponent implements Processor {

    static final Logger logger = LoggerFactory.getLogger(LoggerComponent.class);

    @Property("Logger Level")
    @Default("INFO")
    @Required
    private LoggerLevel level;

    @Override
    public Message apply(Message input) {
        TypedContent content = input.getTypedContent();
        if (content != null) {
            level.log(content.getContent());

        } else {
            level.log(null);
        }
        return input;
    }

    public void setLevel(LoggerLevel level) {
        this.level = level;
    }

}
