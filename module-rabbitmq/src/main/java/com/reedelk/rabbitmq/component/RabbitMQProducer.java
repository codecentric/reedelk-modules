package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.reedelk.rabbitmq.commons.ChannelUtils;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("RabbitMQ Producer")
@Component(service = RabbitMQProducer.class, scope = PROTOTYPE)
public class RabbitMQProducer implements ProcessorSync {

    @Property("Queue Name")
    @Default("queue_outbound")
    @Hint("queue_outbound")
    private String queueName;

    @Property("Host")
    @Default("localhost")
    @Hint("localhost")
    private String host;

    private Channel channel;

    @Reference
    private ConverterService converter;


    @Override
    public Message apply(Message message, FlowContext flowContext) {
        // Convert to Bytes.

        Object payload = message.payload();
        byte[] payloadAsBytes = converter.convert(payload, byte[].class);

        try {
            synchronized (this) {
                channel.basicPublish("", queueName, null, payloadAsBytes);
                return message;
            }
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void initialize() {
        try {
            Connection connection = ConnectionFactoryProvider.connection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void dispose() {
        ChannelUtils.closeSilently(channel);
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setHost(String host) {
        this.host = host;
    }
}