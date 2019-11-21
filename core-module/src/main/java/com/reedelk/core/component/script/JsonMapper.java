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

import static com.reedelk.runtime.api.annotation.AutocompleteType.JSON_SCHEMA;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

// TODO: SHould not be  hidden
@Hidden
@ESBComponent("JSON Mapper")
@Component(service = JsonMapper.class, scope = PROTOTYPE)
public class JsonMapper implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @File
    @Property("Input JSON schema")
    @AutocompleteContext(name = "inputContext", type = JSON_SCHEMA)
    private String inputJsonSchema;

    @File
    @Property("Output JSON schema")
    @AutocompleteContext(name = "outputContext", type = JSON_SCHEMA)
    private String outputJsonSchema;

    @Property("Mapping Script")
    @Variable(variableName = "input", contextName = "inputContext")
    @Variable(variableName = "output", contextName = "outputContext")
    private Script mappingScript;

    private volatile Script enhancer;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        return service.evaluate(enhancer(), flowContext, message, String.class)
                .map(mappedJson -> MessageBuilder.get().json(mappedJson).build())
                .orElse(MessageBuilder.get().empty().build());
    }

    private Script enhancer() {
        if (enhancer == null) {
            synchronized (this) {
                if (enhancer == null) {
                    enhancer = ScriptEnhancer.enhance(mappingScript);
                }
            }
        }
        return enhancer;
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
