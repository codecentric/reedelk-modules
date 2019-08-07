package com.esb.test.utils;

import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;

public class TestComponentWithObjectProperty implements ProcessorSync {

    private TestImplementor config;

    @Override
    public Message apply(Message input) {
        throw new UnsupportedOperationException("Test Only ProcessorSync");
    }

    public TestImplementor getConfig() {
        return config;
    }

    public void setConfig(TestImplementor config) {
        this.config = config;
    }
}
