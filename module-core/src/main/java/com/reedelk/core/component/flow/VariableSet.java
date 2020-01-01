package com.reedelk.core.component.flow;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Variable Set")
@Component(service = VariableSet.class, scope = PROTOTYPE)
public class VariableSet implements ProcessorSync {

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Name")
    @Hint("myVariable")
    private String name;

    @Property("Mime type")
    @Default(MimeType.MIME_TYPE_ANY)
    @MimeTypeCombo
    private String mimeType;

    @Property("Value")
    @Default("#[]")
    @Hint("variable text value")
    private DynamicObject value;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        if (StringUtils.isBlank(name)) {
            throw new ESBException("Variable name must not be empty");
        }

        MimeType mimeType = MimeType.parse(this.mimeType);

        Serializable result = (Serializable) scriptEngine.evaluate(value, mimeType, flowContext, message).orElse(null);

        flowContext.put(name, result);

        return message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(DynamicObject value) {
        this.value = value;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
