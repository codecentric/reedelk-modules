package com.reedelk.core.component.flow;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.message.type.TypedContentFactory;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicObject;
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
            JAVASCRIPT, APPLICATION_XML, APPLICATION_JSON,
            APPLICATION_JAVA, APPLICATION_FORM_URL_ENCODED, MULTIPART_FORM_DATA})
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

        // It is a script, hence we need to evaluate it.
        Object result = scriptEngine.evaluate(value, message, flowContext).orElse(null);
        // TODO: The result here might be typed content ...
        //if (result instanceof TypedContent) {
          //  flowContext.setVariable(name, (TypedContent<?>) result);
        //} else {
            // TODO: Nope typed content factory should also take in consideration that result is typed content
            Type contentType = new Type(mimeType);
            TypedContent<?> content = TypedContentFactory.get().from(result, contentType);
            flowContext.setVariable(name, content);
      //  }
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
