package com.reedelk.scheduler.component;

import com.reedelk.runtime.api.message.DefaultMessageAttributes;

import java.io.Serializable;
import java.util.Map;

public class SchedulerAttributes extends DefaultMessageAttributes {
    public SchedulerAttributes(Map<String, Serializable> attributes) {
        super(attributes);
    }
}
