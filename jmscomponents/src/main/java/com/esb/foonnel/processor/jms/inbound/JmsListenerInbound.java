package com.esb.foonnel.processor.jms.inbound;


import com.esb.foonnel.api.AbstractInbound;
import org.osgi.service.component.annotations.Component;

@Component
public class JmsListenerInbound extends AbstractInbound {

    private String host;
    private String queueName;

    @Override
    public void onStart() {

    }

    @Override
    public void onShutdown() {

    }
}
