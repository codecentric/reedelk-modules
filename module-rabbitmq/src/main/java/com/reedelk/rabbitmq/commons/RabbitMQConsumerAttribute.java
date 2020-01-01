package com.reedelk.rabbitmq.commons;

public interface RabbitMQConsumerAttribute {

    interface Envelope {

        static String name() {
            return "envelope";
        }

        static String deliveryTag() {
            return "deliveryTag";
        }

        static String exchange() {
            return "exchange";
        }

        static String routingKey() {
            return "routingKey";
        }
    }

    interface Properties {

        static String name() {
            return "properties";
        }

        static String contentType() {
            return "contentType";
        }

        static String contentEncoding() {
            return "contentEncoding";
        }

        static String headers() {
            return "headers";
        }

        static String deliveryMode() {
            return "deliveryMode";
        }

        static String priority() {
            return "priority";
        }

        static String correlationId() {
            return "correlationId";
        }

        static String replyTo() {
            return "replyTo";
        }

        static String expiration() {
            return "expiration";
        }

        static String messageId() {
            return "messageId";
        }

        static String timestamp() {
            return "timestamp";
        }

        static String type() {
            return "type";
        }

        static String userId() {
            return "userId";
        }

        static String appId() {
            return "appId";
        }

        static String clusterId() {
            return "clusterId";
        }
    }
}