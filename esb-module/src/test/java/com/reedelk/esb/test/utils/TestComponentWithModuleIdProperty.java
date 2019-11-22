package com.reedelk.esb.test.utils;

import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.system.api.file.ModuleId;

public class TestComponentWithModuleIdProperty implements ProcessorSync {

    private String stringProperty;
    private ModuleId moduleId;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException("Test Only ProcessorSync");
    }

    public ModuleId getModuleId() {
        return moduleId;
    }

    public void setModuleId(ModuleId moduleId) {
        this.moduleId = moduleId;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }
}
