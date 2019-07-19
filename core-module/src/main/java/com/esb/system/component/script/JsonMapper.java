package com.esb.system.component.script;

import com.esb.api.annotation.*;
import com.esb.api.component.Processor;
import com.esb.api.exception.ESBException;
import com.esb.api.message.*;
import com.esb.api.service.ScriptEngineService;
import com.esb.api.service.ScriptExecutionResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("JSON Mapper")
@Component(service = JsonMapper.class, scope = PROTOTYPE)
public class JsonMapper implements Processor {

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
    public Message apply(Message input) {
        try {
            String script = String.format(EXECUTION_SCRIPT_TEMPLATE, mappingScript);

            ScriptExecutionResult result = service.evaluate(input, script, new ComponentVariableBindings(input));

            Object mappedOutput = result.getBindings().get("output");

            TypedContent<Object> content = new MemoryTypedContent<>(mappedOutput, new Type(MimeType.ANY, Object.class));

            input.setTypedContent(content);

            return input;

        } catch (ScriptException e) {
            throw new ESBException(e);
        }
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
            if (message.getTypedContent() != null) {
                put("input", message.getTypedContent().getContent());
            } else {
                put("input", "{}");
            }
            put("output", "{}");
        }
    }
}
