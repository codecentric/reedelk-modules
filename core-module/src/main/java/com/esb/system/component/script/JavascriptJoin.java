package com.esb.system.component.script;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Script;
import com.esb.api.annotation.Variable;
import com.esb.api.component.Join;
import com.esb.api.exception.ESBException;
import com.esb.api.message.Message;
import com.esb.api.message.MessageBuilder;
import com.esb.api.service.ScriptEngineService;
import com.esb.api.service.ScriptExecutionResult;
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

    @Script(inline = false)
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
