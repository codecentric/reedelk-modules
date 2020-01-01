package com.reedelk.rabbitmq.configuration;

import com.reedelk.runtime.api.annotation.Collapsible;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.PropertyInfo;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static java.util.Optional.ofNullable;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Collapsible
@Component(service = CreateQueueConfiguration.class, scope = PROTOTYPE)
public class CreateQueueConfiguration implements Implementor {

    @Property("Create new")
    @PropertyInfo("If true, the consumer will create a new queue with the name provided in the 'Queue Name' " +
            "field (default: false).")
    @When(propertyName = "queueName", propertyValue = When.NOT_SCRIPT)
    private Boolean create;

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

    public void setCreate(Boolean create) {
        this.create = create;
    }

    public static boolean isCreateNew(CreateQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.create))
                .orElse(false);
    }

    public static boolean isDurable(CreateQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.durable))
                .orElse(false);
    }

    public static boolean isExclusive(CreateQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.exclusive))
                .orElse(false);
    }

    public static boolean isAutoDelete(CreateQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.autoDelete))
                .orElse(false);
    }
}
