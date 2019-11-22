package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.Script;

import static java.lang.String.format;

public class ScriptWithMessageAndContext implements FunctionDefinitionBuilder<Script> {

    private final String scriptWithMessageAndContextFunction =
            "function %s(message, context) {\n" +
                    "%s\n" +
                    "};";

    @Override
    public String from(String functionName, Script script) {
        return format(scriptWithMessageAndContextFunction, functionName, script.body());
    }
}
