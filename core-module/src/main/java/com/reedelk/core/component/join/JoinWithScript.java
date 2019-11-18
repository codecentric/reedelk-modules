package com.reedelk.core.component.join;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.Join;
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

import java.util.List;
import java.util.Optional;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Join script")
@Component(service = JoinWithScript.class, scope = PROTOTYPE)
public class JoinWithScript implements Join {

    @Reference
    private ScriptEngineService service;

    @Property("Mime type")
    @Default(MimeType.MIME_TYPE_TEXT_PLAIN)
    @MimeTypeCombo
    private String mimeType;


    @Property("Script")
    @Variable(variableName = "messages")
    private Script script;

    @Override
    public Message apply(List<Message> messagesToJoin, FlowContext flowContext) {
        Optional<Object> result = service.evaluate(script, messagesToJoin, flowContext, Object.class);
        if (result.isPresent()) {
            MimeType mimeType = MimeType.parse(this.mimeType);
            TypedContent<?> typedContent = TypedContentFactory.from(result.get(), mimeType);
            return MessageBuilder.get().typedContent(typedContent).build();
        } else {
            return MessageBuilder.get().empty().build();
        }
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
