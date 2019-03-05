package com.esb.test.utils;

import com.esb.api.component.Processor;
import com.esb.api.message.Message;

import java.util.Map;

public class TestComponentWithMapProperty implements Processor {

    private Map<String, Object> myObjectProperty;

    @Override
    public Message apply(Message input) {
        throw new UnsupportedOperationException("Test Only Processor");
    }

    public Map<String, Object> getMyObjectProperty() {
        return myObjectProperty;
    }

    public void setMyObjectProperty(Map<String, Object> myObjectProperty) {
        this.myObjectProperty = myObjectProperty;
    }
}
