package com.esb.system.component.script;

import com.esb.api.annotation.ESBComponent;
import com.esb.api.annotation.Property;
import com.esb.api.annotation.Script;
import com.esb.api.annotation.Variable;
import com.esb.api.component.ProcessorSync;
import com.esb.api.exception.ESBException;
import com.esb.api.message.Message;
import com.esb.api.message.MessageBuilder;
import com.esb.api.service.ScriptEngineService;
import com.esb.api.service.ScriptExecutionResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Javascript")
@Component(service = JavascriptComponent.class, scope = PROTOTYPE)
public class JavascriptComponent implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Script(inline = false)
    @Property("Script")
    @Variable(variableName = "payload")
    private String script;

    @Override
    public Message apply(Message input) {
        try {
            ScriptExecutionResult result = service.evaluate(input, script, new ComponentVariableBindings(input));

            return MessageBuilder.get().javaObject(result.getObject()).build();
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
    }

    public void setScript(String script) {
        this.script = script;
    }

    class ComponentVariableBindings extends SimpleBindings {
        ComponentVariableBindings(Message message) {
            if (message.getTypedContent() != null) {
                put("payload", message.getTypedContent().content());
            } else {
                put("payload", null);
            }
        }
    }
}
