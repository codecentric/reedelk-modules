package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.Script;

public class EvaluateScriptFunctionWithMessagesAndContext implements FunctionDefinitionBuilder<Script> {

    private static final String EVALUATE_SCRIPT_FUNCTION =
            "var %s = function(messages, context) {\n" +
                    "%s\n" +
                    "};";

    @Override
    public String from(String functionName, Script script) {
        String functionBody = ScriptUtils.unwrap(script.scriptBody());
        return String.format(EVALUATE_SCRIPT_FUNCTION, functionName, functionBody);
    }
}