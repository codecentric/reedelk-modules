package com.esb.system.component.script;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.component.Processor;
import com.esb.api.exception.ESBException;
import com.esb.api.message.*;
import com.esb.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Javascript")
@Component(service = JavascriptComponent.class, scope = PROTOTYPE)
public class JavascriptComponent implements Processor {

    @Reference
    private ScriptEngineService service;

    @Property("Script")
    private String script;

    @Override
    public Message apply(Message input) {
        try {
            Object result = service.evaluate(input, script);
            TypedContent<Object> content = new MemoryTypedContent<>(result, new Type(MimeType.ANY, Object.class));
            input.setTypedContent(content);
            return input;
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
    }

    public void setScript(String script) {
        this.script = script;
    }
}
