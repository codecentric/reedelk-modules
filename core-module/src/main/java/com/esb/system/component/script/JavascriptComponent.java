package com.esb.system.component.script;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.component.Processor;
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
    private ScriptEngineService scriptEngineService;

    @Property("Script")
    private String script;

    @Override
    public Message apply(Message input) {
        try {
            Object result = scriptEngineService.evaluate(input, script, String.class);
            TypedContent<Object> content = new MemoryTypedContent<>(result, new Type(MimeType.TEXT, Object.class));
            input.setTypedContent(content);
            return input;
        } catch (ScriptException e) {
            return input;
        }
    }

    public void setScript(String script) {
        this.script = script;
    }
}
