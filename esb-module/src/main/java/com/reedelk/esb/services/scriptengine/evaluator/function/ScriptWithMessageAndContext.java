package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptBlock;

import static java.lang.String.format;

public class ScriptWithMessageAndContext implements FunctionDefinitionBuilder<Script> {

    private static final String TEMPLATE =
            "function %s(message, context) {\n" +
                    "%s\n" +
                    "};";

    @Override
    public String from(ScriptBlock scriptBlock) {
        Script script = (Script) scriptBlock;
        return format(TEMPLATE, script.functionName(), script.body());
    }
}
