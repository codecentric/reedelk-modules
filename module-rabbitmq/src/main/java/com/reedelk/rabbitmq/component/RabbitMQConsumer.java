package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.reedelk.rabbitmq.commons.ChannelUtils;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.Map;

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


    @Override
    public void onStart() {

        try {
            Connection connection = ConnectionFactoryProvider.connection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                // Message Attributes
                Map<String, Object> headers = delivery.getProperties().getHeaders();


                String message = new String(delivery.getBody(), "UTF-8");

                Message inboundMessage = MessageBuilder.get()
                        .withText(message)
                        .build();

                onEvent(inboundMessage, new OnResult() {
                    @Override
                    public void onResult(Message message, FlowContext flowContext) {

                    }

                    @Override
                    public void onError(Throwable throwable, FlowContext flowContext) {

                    }
                });
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdown() {
        ChannelUtils.closeSilently(channel);
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