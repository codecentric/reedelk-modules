package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.reedelk.rabbitmq.commons.ChannelUtils;
import com.reedelk.rabbitmq.commons.ConnectionFactoryProvider;
import com.reedelk.rabbitmq.commons.ConsumerDeliverCallback;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.exception.ESBException;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("RabbitMQ Consumer")
@Component(service = RabbitMQConsumer.class, scope = PROTOTYPE)
public class RabbitMQConsumer extends AbstractInbound {

    @Property("Host")
    @Default("localhost")
    @Hint("localhost")
    private String host;

    @Property("Queue Name")
    @Default("queue_inbound")
    @Hint("queue_inbound")
    private String queueName;

    @Property("Queue Durable")
    private Boolean queueDurable;
    @Property("Queue Exclusive")
    private Boolean queueExclusive;
    @Property("Queue Auto Delete")
    private Boolean queueAutoDelete;

    private Channel channel;
    private Connection connection;

    @Override
    public void onStart() {
        try {
            connection = ConnectionFactoryProvider.connection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicConsume(queueName, true, new ConsumerDeliverCallback(this), consumerTag -> {
            });
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void onShutdown() {
        ChannelUtils.closeSilently(channel);
        ChannelUtils.closeSilently(connection);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setQueueDurable(Boolean queueDurable) {
        this.queueDurable = queueDurable;
    }

    public void setQueueExclusive(Boolean queueExclusive) {
        this.queueExclusive = queueExclusive;
    }

    public void setQueueAutoDelete(Boolean queueAutoDelete) {
        this.queueAutoDelete = queueAutoDelete;
    }
}