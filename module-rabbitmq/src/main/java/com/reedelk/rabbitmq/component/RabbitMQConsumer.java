package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("RabbitMQ Consumer")
@Component(service = RabbitMQConsumer.class, scope = PROTOTYPE)
public class RabbitMQConsumer extends AbstractInbound {

    @Property("Host")
    private String host;
    @Property("Queue Name")
    private String queueName;
    @Property("Queue Durable")
    private Boolean queueDurable;
    @Property("Queue Exclusive")
    private Boolean queueExclusive;
    @Property("Queue Auto Delete")
    private Boolean queueAutoDelete;


    private Connection connection;

    @Override
    public void onStart() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

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
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}