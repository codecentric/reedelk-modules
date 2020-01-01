package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.reedelk.rabbitmq.commons.ChannelUtils;
import com.reedelk.rabbitmq.commons.ConnectionFactoryProvider;
import com.reedelk.rabbitmq.commons.ConsumerCancelCallback;
import com.reedelk.rabbitmq.commons.ConsumerDeliverCallback;
import com.reedelk.rabbitmq.configuration.ConnectionFactoryConfiguration;
import com.reedelk.rabbitmq.configuration.DeclareQueueConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.content.MimeType;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotBlank;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("RabbitMQ Consumer")
@Component(service = RabbitMQConsumer.class, scope = PROTOTYPE)
public class RabbitMQConsumer extends AbstractInbound {

    @Property("Connection Configuration")
    private ConnectionFactoryConfiguration configuration;

    @Property("Connection URI")
    @PropertyInfo("Configure a connection using the provided AMQP URI " +
            "containing the connection data.")
    @Hint("amqp://guest:guest@localhost:5672/")
    @Default("amqp://guest:guest@localhost:5672/")
    @When(propertyName = "configuration", propertyValue = When.NULL)
    @When(propertyName = "configuration", propertyValue = "{'ref': '" + When.BLANK + "'}")
    private String connectionURI;

    @Property("Queue Name")
    @PropertyInfo("Defines the name of the queue this consumer will be consuming messages from.")
    @Default("queue_inbound")
    @Hint("queue_inbound")
    private String queueName;

    @Property("Consumed Content Mime Type")
    @PropertyInfo("The Mime Type of the consumed content allows to create " +
            "a flow message with a suitable content type for the following flow components " +
            "(e.g a 'text/plain' mime type converts the consumed content to a string, " +
            "a 'application/octet-stream' keeps the consumed content as byte array).")
    @MimeTypeCombo
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    private String messageMimeType;

    @Property("Declare queue")
    @PropertyInfo("If true, the consumer will declare a new queue to be used for consuming " +
            "messages from (default: false).")
    private Boolean declareQueue;

    @Property("Declare Queue Configuration")
    @When(propertyName = "declareQueue", propertyValue = "true")
    private DeclareQueueConfiguration declareConfig;

    private Channel channel;
    private Connection connection;

    @Override
    public void onStart() {
        requireNotBlank(queueName, "Queue Name must not be empty");
        boolean shouldDeclareQueue = shouldDeclareQueue();

        if (configuration == null) {
            requireNotBlank(connectionURI, "Connection URI must not be empty");
            connection = ConnectionFactoryProvider.from(connectionURI);
        } else {
            connection = ConnectionFactoryProvider.from(configuration);
        }

        MimeType queueMessageContentType = MimeType.parse(messageMimeType);

        try {
            channel = connection.createChannel();
            createQueueIfNeeded(shouldDeclareQueue);
            channel.basicConsume(
                    queueName,
                    true,
                    new ConsumerDeliverCallback(this, queueMessageContentType),
                    new ConsumerCancelCallback());
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void onShutdown() {
        ChannelUtils.closeSilently(channel);
        ChannelUtils.closeSilently(connection);
    }

    public void setConfiguration(ConnectionFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setMessageMimeType(String messageMimeType) {
        this.messageMimeType = messageMimeType;
    }

    public void setDeclareQueue(Boolean declareQueue) {
        this.declareQueue = declareQueue;
    }

    public void setDeclareConfig(DeclareQueueConfiguration declareConfig) {
        this.declareConfig = declareConfig;
    }

    public void setConnectionURI(String connectionURI) {
        this.connectionURI = connectionURI;
    }

    private boolean shouldDeclareQueue() {
        return declareQueue == null ? false : declareQueue;
    }

    private void createQueueIfNeeded(boolean shouldDeclareQueue) throws IOException {
        if (shouldDeclareQueue) {
            boolean durable = DeclareQueueConfiguration.isDurable(declareConfig);
            boolean exclusive = DeclareQueueConfiguration.isExclusive(declareConfig);
            boolean autoDelete = DeclareQueueConfiguration.isAutoDelete(declareConfig);
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
        }
    }
}