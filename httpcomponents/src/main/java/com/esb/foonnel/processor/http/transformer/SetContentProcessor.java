package com.esb.foonnel.processor.http.transformer;

import com.esb.foonnel.api.Message;
import com.esb.foonnel.api.Processor;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = SetContentProcessor.class)
public class SetContentProcessor implements Processor {

    public String content;

    @Override
    public Message apply(Message input) {
        return null;
    }
}
