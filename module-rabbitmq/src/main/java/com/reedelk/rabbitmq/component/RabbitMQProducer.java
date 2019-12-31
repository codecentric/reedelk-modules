package com.reedelk.rabbitmq.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;

@ESBComponent("RabbitMQ Producer")
public class RabbitMQProducer implements ProcessorSync {

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        return null;
    }
}