package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Script;
import com.reedelk.runtime.api.annotation.Variable;
import com.reedelk.runtime.api.component.Join;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptException;
import java.util.List;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Javascript Join")
@Component(service = JavascriptJoin.class, scope = PROTOTYPE)
public class JavascriptJoin implements Join {

    @Reference
    private ScriptEngineService service;

    @Script
    @Property("Script")
    @Variable(variableName = "messages")
    private String script;

    private final String BASE_TEMPLATE = "function merge(object1, object2) {\n" +
            "    for (var attrname in object1) { \n" +
            "        object2[attrname] = object1[attrname]; \n" +
            "    }\n" +
            "}\n\n" +
            "%s";

    @Override
    public Message apply(List<Message> messagesToJoin) {
        try {
            String actualScript = String.format(BASE_TEMPLATE, script);
            ScriptExecutionResult result = service.evaluate(messagesToJoin, actualScript);
            return MessageBuilder.get().javaObject(result.getObject()).build();
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
    }

    public void setScript(String script) {
        this.script = script;
    }
}
