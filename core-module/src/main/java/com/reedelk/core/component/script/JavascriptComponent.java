package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Script;
import com.reedelk.runtime.api.annotation.Variable;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
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
                put("payload", message.getTypedContent().asObject());
            } else {
                put("payload", null);
            }
        }
    }
}
