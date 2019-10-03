package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Variable;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Javascript")
@Component(service = JavascriptComponent.class, scope = PROTOTYPE)
public class JavascriptComponent implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("Script")
    @Variable(variableName = "payload")
    private Script script;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Optional<Object> evaluated = service.evaluate(script, message, flowContext, Object.class);
        if (evaluated.isPresent()) {
            return MessageBuilder.get().javaObject(evaluated.get()).build();
        } else {
            return MessageBuilder.get().empty().build();
        }
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
