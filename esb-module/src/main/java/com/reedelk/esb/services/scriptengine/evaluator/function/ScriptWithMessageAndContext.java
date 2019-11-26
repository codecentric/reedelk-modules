package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.Script;

import static java.lang.String.format;

public class ScriptWithMessageAndContext implements FunctionDefinitionBuilder<Script> {

    private static final String TEMPLATE =
            "function %s(message, context) {\n" +
                    "%s\n" +
                    "};";

    @Override
    public String from(Script script) {
        return format(TEMPLATE, script.functionName(), script.body());
    }
}
