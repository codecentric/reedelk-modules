package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.Script;

import static java.lang.String.format;

public class ScriptWithMessagesAndContext implements FunctionDefinitionBuilder<Script> {

    private final String scriptWithMessagesAndContextFunction =
            "function %s(messages, context) {\n" +
                    "%s\n" +
                    "};";

    @Override
    public String from(String functionName, Script script) {
        return format(scriptWithMessagesAndContextFunction, functionName, script.body());
    }
}