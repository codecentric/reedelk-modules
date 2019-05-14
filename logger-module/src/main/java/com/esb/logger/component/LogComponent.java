package com.esb.logger.component;

import com.esb.api.annotation.DefaultValue;
import com.esb.api.annotation.DisplayName;
import com.esb.api.annotation.EsbComponent;
import com.esb.api.annotation.Required;
import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import com.esb.api.message.TypedContent;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@EsbComponent
@Component(service = LogComponent.class, scope = PROTOTYPE)
public class LogComponent implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(LogComponent.class);

    @Required
    @DisplayName("Log Level")
    @DefaultValue(stringValue = "INFO")
    private String level;

    @Override
    public Message apply(Message input) {
        TypedContent content = input.getTypedContent();
        if (content != null) {
            LogLevel.from(level).log(content.getContent());
        } else {
            LogLevel.from(level).log(null);
        }
        return input;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }


    enum LogLevel implements MessageLogger {

        INFO("INFO") {
            @Override
            public void log(Object message) {
                logger.info(asLoggableString(message));
            }
        },
        DEBUG("DEBUG") {
            @Override
            public void log(Object message) {
                logger.debug(asLoggableString(message));
            }
        },
        WARN("WARN") {
            @Override
            public void log(Object message) {
                logger.warn(asLoggableString(message));
            }
        },
        ERROR("ERROR") {
            @Override
            public void log(Object message) {
                logger.error(asLoggableString(message));
            }
        },
        TRACE("TRACE") {
            @Override
            public void log(Object message) {
                logger.trace(asLoggableString(message));
            }
        };

        private String levelName;


        LogLevel(String levelName) {
            this.levelName = levelName;
        }

        static LogLevel from(String stringValue) {
            for (LogLevel level : values()) {
                if (level.levelName.equalsIgnoreCase(stringValue)) {
                    return level;
                }
            }
            return INFO;
        }

        private static String asLoggableString(Object message) {
            return message == null ? null : message.toString();
        }
    }

    interface MessageLogger {
        void log(Object message);
    }


}
