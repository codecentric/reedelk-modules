package com.esb.fonnel.processor.jms.inbound;


import com.esb.foonnel.domain.AbstractInbound;
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
