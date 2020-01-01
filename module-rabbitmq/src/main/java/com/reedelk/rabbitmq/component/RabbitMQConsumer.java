package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.reedelk.rabbitmq.commons.*;
import com.reedelk.rabbitmq.configuration.ConnectionFactoryConfiguration;
import com.reedelk.rabbitmq.configuration.CreateQueueConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.content.MimeType;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotBlank;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("RabbitMQ Consumer")
@Component(service = RabbitMQConsumer.class, scope = PROTOTYPE)
public class RabbitMQConsumer extends AbstractInbound {

    @Property("Connection Config")
    private ConnectionFactoryConfiguration configuration;

    @Property("Connection URI")
    @PropertyInfo("Configure a connection using the provided AMQP URI " +
            "containing the connection data.")
    @Hint("amqp://guest:guest@localhost:5672")
    @Default("amqp://guest:guest@localhost:5672")
    @When(propertyName = "configuration", propertyValue = When.NULL)
    @When(propertyName = "configuration", propertyValue = "{'ref': '" + When.BLANK + "'}")
    private String connectionURI;

    @Property("Queue Name")
    @PropertyInfo("Defines the name of the queue this consumer will be consuming messages from.")
    @Hint("queue_inbound")
    private String queueName;

    @Property("Content Mime Type")
    @PropertyInfo("The Mime Type of the consumed content allows to create " +
            "a flow message with a suitable content type for the following flow components " +
            "(e.g a 'text/plain' mime type converts the consumed content to a string, " +
            "a 'application/octet-stream' keeps the consumed content as byte array).")
    @MimeTypeCombo
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    private String messageMimeType;

    @Property("Auto Acknowledge")
    @PropertyInfo("True to immediately consider messages delivered by the broker as soon as the flow starts." +
            "False to acknowledge the message only if the flow executed successfully.")
    @Default("true")
    private boolean autoAck;

    @Property("Create Consumer Queue Settings")
    private CreateQueueConfiguration createQueueConfiguration;

    private Channel channel;
    private Connection connection;

    @Override
    public void onStart() {
        requireNotBlank(queueName, "Queue Name must not be empty");
        if (configuration == null) {
            requireNotBlank(connectionURI, "Connection URI must not be empty");
            connection = ConnectionFactoryProvider.from(connectionURI);
        } else {
            connection = ConnectionFactoryProvider.from(configuration);
        }

        try {
            channel = connection.createChannel();
            createQueueIfNeeded();
            MimeType queueMessageContentType = MimeType.parse(messageMimeType);
            if (autoAck) {
                channel.basicConsume(queueName, true,
                        new ConsumerDeliverCallbackAutoAck( this, queueMessageContentType),
                        new ConsumerCancelCallback());
            } else {
                channel.basicConsume(queueName, false,
                        new ConsumerDeliverCallbackExplicitAck( this, queueMessageContentType, channel),
                        new ConsumerCancelCallback());
            }

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

    public void setCreateQueueConfiguration(CreateQueueConfiguration createQueueConfiguration) {
        this.createQueueConfiguration = createQueueConfiguration;
    }

    public void setConnectionURI(String connectionURI) {
        this.connectionURI = connectionURI;
    }

    public void setAutoAck(Boolean autoAck) {
        this.autoAck = autoAck;
    }

    private boolean shouldDeclareQueue() {
        return ofNullable(createQueueConfiguration)
                .flatMap(createQueueConfiguration ->
                        of(CreateQueueConfiguration.isCreateNew(createQueueConfiguration)))
                .orElse(false);
    }

    private void createQueueIfNeeded() throws IOException {
        boolean shouldDeclareQueue = shouldDeclareQueue();
        if (shouldDeclareQueue) {
            boolean durable = CreateQueueConfiguration.isDurable(createQueueConfiguration);
            boolean exclusive = CreateQueueConfiguration.isExclusive(createQueueConfiguration);
            boolean autoDelete = CreateQueueConfiguration.isAutoDelete(createQueueConfiguration);
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
        }
    }
}