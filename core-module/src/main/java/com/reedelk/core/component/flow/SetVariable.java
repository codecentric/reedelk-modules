package com.reedelk.core.component.flow;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.*;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Variable")
@Component(service = SetVariable.class, scope = PROTOTYPE)
public class SetVariable implements ProcessorSync {

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Name")
    private String name;

    @ScriptInline
    @Property("Value")
    @Hint("variable value")
    private String value;

    @Property("Mime type")
    @Default("NONE")
    private VariableMimeType mimeType;

    @Property("Custom mime type")
    @When(propertyName = "mimeType", propertyValue = "NONE")
    private String mimeTypeValue;


    @Override
    public Message apply(Message input, FlowContext flowContext) {
        if (StringUtils.isBlank(name)) {
            throw new ESBException("Variable name must not be empty");
        }

        MimeType variableMimeType = getMimeType();
        if (ScriptUtils.isScript(value)) {

            try {
                Object result = scriptEngine.evaluate(value, input, flowContext);
                Type contentType = new Type(variableMimeType);
                TypedContent<?> content = TypedContentFactory.get().from(result, contentType);
                flowContext.setVariable(name, content);
            } catch (ScriptException e) {
                throw new ESBException(e);
            }

        } else {
            Type contentType = new Type(variableMimeType, String.class);
            TypedContent<?> content = new StringContent(value, contentType);
            flowContext.setVariable(name, content);
        }
        return input;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setMimeType(VariableMimeType mimeType) {
        this.mimeType = mimeType;
    }

    public void setMimeTypeValue(String mimeTypeValue) {
        this.mimeTypeValue = mimeTypeValue;
    }

    private MimeType getMimeType() {
        if (mimeType == null || VariableMimeType.NONE.equals(mimeType)) {
            // Custom mime type
            return MimeType.parse(mimeTypeValue);
        } else {
            return mimeType.mapped();
        }
    }
}
