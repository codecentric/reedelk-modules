package com.reedelk.rabbitmq.component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.reedelk.rabbitmq.commons.ChannelUtils;
import com.reedelk.rabbitmq.commons.ConnectionFactoryProvider;
import com.reedelk.rabbitmq.configuration.ConnectionFactoryConfiguration;
import com.reedelk.rabbitmq.configuration.QueueConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotBlank;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("RabbitMQ Producer")
@Component(service = RabbitMQProducer.class, scope = PROTOTYPE)
public class RabbitMQProducer implements ProcessorSync {

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

    @Property("Exchange Name")
    @PropertyInfo("The name of the exchange to publish the message to. It might be a dynamic property.")
    @Hint("amq.fanout")
    private DynamicString exchangeName;

    @Property("Queue Name")
    @Hint("queue_outbound")
    private DynamicString queueName;

    @Property("Queue Settings")
    @When(propertyName = "queueName", propertyValue = When.NOT_SCRIPT)
    private QueueConfiguration queueConfiguration;

    @Reference
    private ConverterService converter;
    @Reference
    private ScriptEngineService scriptEngine;

    private Channel channel;
    private Connection connection;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String queueName = scriptEngine.evaluate(this.queueName, flowContext, message)
                .orElseThrow(() -> new ESBException("Queue name not found"));

        String exchangeName = scriptEngine.evaluate(this.exchangeName, flowContext, message)
                .orElse(StringUtils.EMPTY);

        Object payload = message.payload();
        byte[] payloadAsBytes = converter.convert(payload, byte[].class);

        try {
            synchronized (this) {
                // Only one Thread should publish data, otherwise we might have
                // out of order arrivals.
                channel.basicPublish(exchangeName, queueName, null, payloadAsBytes);
                return message;
            }
        } catch (IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void initialize() {
        if (configuration == null) {
            requireNotBlank(connectionURI, "Connection URI must not be empty");
            connection = ConnectionFactoryProvider.from(connectionURI);
        } else {
            connection = ConnectionFactoryProvider.from(configuration);
        }
        try {
            channel = connection.createChannel();
            createQueueIfNeeded();
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

    public void setConnectionURI(String connectionURI) {
        this.connectionURI = connectionURI;
    }

    public void setQueueName(DynamicString queueName) {
        this.queueName = queueName;
    }

    public void setExchangeName(DynamicString exchangeName) {
        this.exchangeName = exchangeName;
    }

    public void setQueueConfiguration(QueueConfiguration queueConfiguration) {
        this.queueConfiguration = queueConfiguration;
    }

    private boolean shouldDeclareQueue() {
        return ofNullable(queueConfiguration)
                .flatMap(queueConfiguration ->
                        of(QueueConfiguration.isCreateNew(queueConfiguration)))
                .orElse(false);
    }

    private void createQueueIfNeeded() throws IOException {
        // If it is a script we cannot create it.
        if (queueName.isScript()) return;

        boolean shouldDeclareQueue = shouldDeclareQueue();
        if (shouldDeclareQueue) {
            boolean durable = QueueConfiguration.isDurable(queueConfiguration);
            boolean exclusive = QueueConfiguration.isExclusive(queueConfiguration);
            boolean autoDelete = QueueConfiguration.isAutoDelete(queueConfiguration);
            channel.queueDeclare(queueName.body(), durable, exclusive, autoDelete, null);
        }
    }
}