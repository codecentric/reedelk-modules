package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.MimeTypeCombo;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.message.content.factory.TypedContentFactory;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Script")
@Component(service = ScriptEvaluator.class, scope = PROTOTYPE)
public class ScriptEvaluator implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("Mime type")
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    @MimeTypeCombo
    private String mimeType;

    @Property("Script")
    private Script script;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        MimeType mimeType = MimeType.parse(this.mimeType);

        Object evaluated = service.evaluate(script, Object.class, flowContext, message).orElse(null);

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
