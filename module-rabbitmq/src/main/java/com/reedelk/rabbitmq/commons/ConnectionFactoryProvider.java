package com.reedelk.rabbitmq.commons;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.reedelk.rabbitmq.configuration.ConnectionFactoryConfiguration;
import com.reedelk.runtime.api.exception.ESBException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import static com.reedelk.rabbitmq.configuration.ConnectionFactoryConfiguration.*;

public class ConnectionFactoryProvider {

    public static Connection from(String uri) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setUri(uri);
            return connectionFactory.newConnection();
        } catch (IOException |
                TimeoutException |
                NoSuchAlgorithmException |
                KeyManagementException |
                URISyntaxException e) {
            throw new ESBException(e);
        }
    }

    public static Connection from(ConnectionFactoryConfiguration configuration) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUsername(userName(configuration));
            factory.setPassword(password(configuration));
            factory.setVirtualHost(virtualHost(configuration));
            factory.setHost(hostName(configuration));
            factory.setPort(port(configuration));
            factory.setAutomaticRecoveryEnabled(isAutomaticRecovery(configuration));
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new ESBException(e);
        }
    }
}
