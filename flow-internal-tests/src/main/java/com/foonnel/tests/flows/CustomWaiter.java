package com.foonnel.tests.flows;

import com.esb.foonnel.api.component.Processor;
import com.esb.foonnel.api.message.Message;
import org.osgi.service.component.annotations.Component;

import java.util.Random;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = CustomWaiter.class, scope = PROTOTYPE)
public class CustomWaiter implements Processor {
    @Override
    public Message apply(Message message) {
        try {
            Thread.sleep(new Random().nextInt(2) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message;
    }
}
