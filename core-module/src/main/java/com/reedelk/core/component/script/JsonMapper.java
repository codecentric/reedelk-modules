package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.*;
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

@ESBComponent("JSON Mapper")
@Component(service = JsonMapper.class, scope = PROTOTYPE)
public class JsonMapper implements ProcessorSync {

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

    @Property("Mapping Script")
    @Variable(variableName = "input", contextName = "inputContext")
    @Variable(variableName = "output", contextName = "outputContext")
    private Script mappingScript;

    private volatile ScriptEnhancer enhancer;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        // TODO: Test what happens if a function with the same name and id gets evaluated twice!!
        //  does it override the original definition?

        if (enhancer == null) {
            synchronized (this) {
                if (enhancer == null) {
                    enhancer = new ScriptEnhancer(mappingScript);
                }
            }
        }

        Optional<String> mappedJson = service.evaluate(enhancer, message, flowContext, String.class);
        if (!mappedJson.isPresent()) {
            return MessageBuilder.get().empty().build();
        } else {
            return MessageBuilder.get().json(mappedJson.get()).build();
        }
    }

    public void setInputJsonSchema(String inputJsonSchema) {
        this.inputJsonSchema = inputJsonSchema;
    }

    public void setOutputJsonSchema(String outputJsonSchema) {
        this.outputJsonSchema = outputJsonSchema;
    }

    public void setMappingScript(Script mappingScript) {
        this.mappingScript = mappingScript;
    }
}
