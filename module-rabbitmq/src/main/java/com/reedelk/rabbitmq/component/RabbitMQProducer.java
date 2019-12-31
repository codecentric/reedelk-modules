package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.reedelk.rabbitmq.commons.ChannelUtils;
import com.reedelk.rabbitmq.commons.ConnectionFactoryProvider;
import com.reedelk.rabbitmq.configuration.ConnectionFactoryConfiguration;
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

    @Property("Connection Configuration")
    private ConnectionFactoryConfiguration configuration;

    @Property("Queue Name")
    @Default("queue_outbound")
    @Hint("queue_outbound")
    private String queueName; // TODO: Should be dynamic

    @Property("Exchange")
    @Default("amq.direct")
    @Hint("amq.direct")
    private String exchange;  // TODO: Should be dynamic

    @Reference
    private ConverterService converter;

    private Channel channel;
    private Connection connection;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Object payload = message.payload();
        byte[] payloadAsBytes = converter.convert(payload, byte[].class);

        try {
            synchronized (this) {
                channel.basicPublish(exchange, queueName, null, payloadAsBytes);
                return message;
            }
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void initialize() {
        try {
            connection = ConnectionFactoryProvider.connection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void dispose() {
        ChannelUtils.closeSilently(channel);
        ChannelUtils.closeSilently(connection);
    }

    public void setConfiguration(ConnectionFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}