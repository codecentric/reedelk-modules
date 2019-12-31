package com.reedelk.rabbitmq.commons;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ChannelUtils {

    private ChannelUtils() {
    }

    public static void closeSilently(Channel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                // Nothing to do
            }
        }
    }

    public static void closeSilently(Connection connection) {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (IOException e) {
                // Nothing to do
            }
        }
    }
}
