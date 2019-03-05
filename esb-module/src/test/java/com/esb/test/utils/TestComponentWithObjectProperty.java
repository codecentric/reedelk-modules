package com.esb.test.utils;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;

public class TestComponentWithObjectProperty implements Processor {

    private TestImplementor config;

    @Override
    public Message apply(Message input) {
        throw new UnsupportedOperationException("Test Only Processor");
    }

    public TestImplementor getConfig() {
        return config;
    }

    public void setConfig(TestImplementor config) {
        this.config = config;
    }
}
