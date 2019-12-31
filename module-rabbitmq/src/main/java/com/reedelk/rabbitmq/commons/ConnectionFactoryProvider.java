package com.reedelk.rabbitmq.commons;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.reedelk.runtime.api.exception.ESBException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConnectionFactoryProvider {

    private static final ConnectionFactory connectionFactory = new ConnectionFactory();

    private static ConnectionFactory get() {
        return connectionFactory;
    }

    public static synchronized Connection connection() {
        try {
            return connectionFactory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new ESBException(e);
        }
    }
}
