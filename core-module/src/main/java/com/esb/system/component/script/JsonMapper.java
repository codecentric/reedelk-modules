package com.esb.system.component.script;

import com.esb.api.annotation.*;
import com.esb.api.component.Processor;
import com.esb.api.message.Message;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("JSON Mapper")
@Component(service = JsonMapper.class, scope = PROTOTYPE)
public class JsonMapper implements Processor {

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
    @AutocompleteVariable(variableName = "input", initValue = "{}", contextName = "inputContext")
    @AutocompleteVariable(variableName = "output", initValue = "{}", contextName = "outputContext")
    private String mappingScript;

    @Override
    public Message apply(Message input) {
        return null;
    }
}
