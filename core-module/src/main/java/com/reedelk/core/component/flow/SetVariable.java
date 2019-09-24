package com.reedelk.core.component.flow;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.*;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.reedelk.runtime.api.message.type.MimeType.Literal;
import static com.reedelk.runtime.api.message.type.MimeType.Literal.*;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Variable")
@Component(service = SetVariable.class, scope = PROTOTYPE)
public class SetVariable implements ProcessorSync {

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Name")
    @Hint("myVariableName")
    private String name;

    @Property("Mime type")
    @Default(ANY)
    @Combo(editable = true, comboValues = {
            ANY, XML, CSS, JSON, HTML, TEXT, RSS, ATOM, BINARY, Literal.UNKNOWN,
            JAVASCRIPT, APPLICATION_XML, MULTIPART_MIXED, APPLICATION_JSON,
            APPLICATION_JAVA, MULTIPART_RELATED, MULTIPART_FORM_DATA, MULTIPART_X_MIXED_REPLACE})
    private String mimeType;

    @Property("Value")
    @Default("#[]")
    @Hint("variable text value")
    private DynamicValue value;


    @Override
    public Message apply(Message message, FlowContext flowContext) {
        if (StringUtils.isBlank(name)) {
            throw new ESBException("Variable name must not be empty");
        }

        MimeType mimeType = MimeType.parse(this.mimeType);

        if (value.isScript()) {
            // It is a script, hence we need to evaluate it.
            Object result = scriptEngine.evaluate(value, message, flowContext);

            Type contentType = new Type(mimeType);
            TypedContent<?> content = TypedContentFactory.get().from(result, contentType);
            flowContext.setVariable(name, content);

        } else {
            // Since it is only a text value, we set the
            // class of the content as String.class.
            Type contentType = new Type(mimeType, String.class);
            TypedContent<?> content = new StringContent(value.getBody(), contentType);
            flowContext.setVariable(name, content);
        }

        return message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(DynamicValue value) {
        this.value = value;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
