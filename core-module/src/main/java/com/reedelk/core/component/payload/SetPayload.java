package com.reedelk.core.component.payload;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.message.type.TypedContentFactory;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicObject;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.reedelk.runtime.api.message.type.MimeType.Literal.*;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Payload")
@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements ProcessorSync {

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Mime type")
    @Default(ANY)
    @Combo(editable = true, comboValues = {
            ANY, XML, CSS, JSON, HTML, TEXT, RSS, ATOM, BINARY, MimeType.Literal.UNKNOWN,
            JAVASCRIPT, APPLICATION_XML, MULTIPART_MIXED, APPLICATION_JSON,
            APPLICATION_JAVA, MULTIPART_RELATED, MULTIPART_FORM_DATA, MULTIPART_X_MIXED_REPLACE})
    private String mimeType;

    @Property("Message Payload")
    @Default("#[]")
    @Hint("payload text value")
    private DynamicObject payload;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        MimeType mimeType = MimeType.parse(this.mimeType);

        // It is a script, hence we need to evaluate it.
        Object result = scriptEngine.evaluate(payload, message, flowContext).orElse(null);
        Type contentType = new Type(mimeType);
        TypedContent<?> content = TypedContentFactory.get().from(result, contentType);
        return MessageBuilder.get().typedContent(content).build();
    }

    public void setPayload(DynamicObject payload) {
        this.payload = payload;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}

