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
@Component(service = ProducerQueueConfiguration.class, scope = PROTOTYPE)
public class ProducerQueueConfiguration implements Implementor {

    @Property("Create new queue")
    @PropertyInfo("If true, a queue with the name provided in the 'Queue Name' field will be created in the broker." +
            "If false the queue is considered already defined in the broker and an error will be thrown if the" +
            " queue does not exists (default: false).")
    @When(propertyName = "queueName", propertyValue = When.NOT_SCRIPT)
    private Boolean create;

    @Property("Durable after restart")
    @PropertyInfo("If true the queue will survive a server restart (default: false).")
    @When(propertyName = "create", propertyValue = "true")
    private Boolean durable;

    @Property("Exclusive to connection")
    @PropertyInfo("If true the use of the queue will be restricted to this connection (default: false).")
    @When(propertyName = "create", propertyValue = "true")
    private Boolean exclusive;

    @Property("Auto Delete")
    @PropertyInfo("If true the server will delete the queue when it is no longer in use (default: false).")
    @When(propertyName = "create", propertyValue = "true")
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

    public static boolean isCreateNew(ProducerQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.create))
                .orElse(false);
    }

    public static boolean isDurable(ProducerQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.durable))
                .orElse(false);
    }

    public static boolean isExclusive(ProducerQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.exclusive))
                .orElse(false);
    }

    public static boolean isAutoDelete(ProducerQueueConfiguration configuration) {
        return ofNullable(configuration)
                .flatMap(config -> ofNullable(config.autoDelete))
                .orElse(false);
    }
}
