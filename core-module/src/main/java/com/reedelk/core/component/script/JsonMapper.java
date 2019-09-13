package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.SimpleBindings;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("JSON Mapper")
@Component(service = JsonMapper.class, scope = PROTOTYPE)
public class JsonMapper implements ProcessorSync {

    private static final String EXECUTION_SCRIPT_TEMPLATE =
            "input = JSON.parse(input); " +
                    "output = JSON.parse('{}'); " +
                    "%s" +
                    "output = JSON.stringify(output);";

    @Reference
    private ScriptEngineService service;

    @File
    @Property("Input JSON schema")
    @AutocompleteContext(name = "inputContext", type = AutocompleteType.JSON_SCHEMA)
    private String inputJsonSchema;

    @File
    @Property("Output JSON schema")
    @AutocompleteContext(name = "outputContext", type = AutocompleteType.JSON_SCHEMA)
    private String outputJsonSchema;

    @Script
    @Property("Mapping Script")
    @Variable(variableName = "input", contextName = "inputContext")
    @Variable(variableName = "output", contextName = "outputContext")
    private String mappingScript;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        String script = String.format(EXECUTION_SCRIPT_TEMPLATE, mappingScript);

        ScriptExecutionResult result = service.evaluate(script, message, new ComponentVariableBindings(message));

        Object mappedOutput = result.getBindings().get("output");

        return MessageBuilder.get().javaObject(mappedOutput).build();
    }

    public void setInputJsonSchema(String inputJsonSchema) {
        this.inputJsonSchema = inputJsonSchema;
    }

    public void setOutputJsonSchema(String outputJsonSchema) {
        this.outputJsonSchema = outputJsonSchema;
    }

    public void setMappingScript(String mappingScript) {
        this.mappingScript = mappingScript;
    }

    class ComponentVariableBindings extends SimpleBindings {
        ComponentVariableBindings(Message message) {
            if (message.getContent() != null) {
                put("input", message.getContent().data());
            } else {
                put("input", "{}");
            }
            put("output", "{}");
        }
    }
}
