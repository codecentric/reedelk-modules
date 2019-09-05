package com.reedelk.esb.test.utils;

import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;

public class TestComponentWithObjectProperty implements ProcessorSync {

    private TestImplementor config;

    @Override
    public Message apply(Message input, Context context) {
        throw new UnsupportedOperationException("Test Only ProcessorSync");
    }

    public TestImplementor getConfig() {
        return config;
    }

    public void setConfig(TestImplementor config) {
        this.config = config;
    }
}
