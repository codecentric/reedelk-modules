package com.reedelk.core.component.payload;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.message.type.TypedContentFactory;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Payload")
@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements ProcessorSync {

    @Reference
    private ScriptEngineService scriptEngine;

    @ScriptInline
    @Property("Message Payload")
    @Default("#[]")
    @Hint("payload text value")
    private String payload;

    @Override
    public Message apply(Message input, FlowContext flowContext) {

        if (ScriptUtils.isScript(payload)) {

            try {
                String realScript = ScriptUtils.unwrap(payload);
                Object result = scriptEngine.evaluate(realScript, input, flowContext);
                Type contentType = new Type(MimeType.ANY);
                TypedContent<?> content = TypedContentFactory.get().from(result, contentType);
                return MessageBuilder.get()
                        .typedContent(content)
                        .build();

            } catch (ScriptException e) {
                throw new ESBException(e);
            }

        } else {
            return MessageBuilder.get()
                    .text(payload)
                    .build();
        }
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}

