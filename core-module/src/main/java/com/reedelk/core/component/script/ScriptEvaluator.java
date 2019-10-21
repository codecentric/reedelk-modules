package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.message.content.factory.TypedContentFactory;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.reedelk.runtime.api.message.content.MimeType.Literal.*;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Script")
@Component(service = ScriptEvaluator.class, scope = PROTOTYPE)
public class ScriptEvaluator implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("Mime type")
    @Default(ANY)
    @Combo(editable = true, comboValues = {
            ANY, XML, CSS, JSON, HTML, TEXT, RSS, ATOM, BINARY, MimeType.Literal.UNKNOWN,
            JAVASCRIPT, APPLICATION_XML, APPLICATION_JSON, APPLICATION_JAVA, MULTIPART_FORM_DATA})
    private String mimeType;

    @Property("Script")
    @Variable(variableName = "payload")
    private Script script;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        MimeType mimeType = MimeType.parse(this.mimeType);

        Object evaluated = service.evaluate(script, message, flowContext, Object.class).orElse(null);;

        TypedContent<?> content = TypedContentFactory.from(evaluated, mimeType);

        return MessageBuilder.get().typedContent(content).build();
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
