package com.reedelk.rabbitmq.configuration;

import com.reedelk.runtime.api.annotation.Collapsible;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.PropertyInfo;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static java.util.Optional.ofNullable;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = DeclareQueueConfiguration.class, scope = PROTOTYPE)
public class DeclareQueueConfiguration implements Implementor {

    @Property("Durable")
    @PropertyInfo("If true the queue will survive a server restart (default: false).")
    private Boolean durable;

    @Property("Exclusive")
    @PropertyInfo("If true the use of the queue will be restricted to this connection (default: false).")
    private Boolean exclusive;

    @Property("Auto Delete")
    @PropertyInfo("If true the server will delete the queue when it is no longer in use (default: false).")
    private Boolean autoDelete;

    public void setDurable(Boolean durable) {
        this.durable = durable;
    }

    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    public void setAutoDelete(Boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public static boolean isDurable(DeclareQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.durable))
                .orElse(false);
    }

    public static boolean isExclusive(DeclareQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.exclusive))
                .orElse(false);
    }

    public static boolean isAutoDelete(DeclareQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.autoDelete))
                .orElse(false);
    }
}
