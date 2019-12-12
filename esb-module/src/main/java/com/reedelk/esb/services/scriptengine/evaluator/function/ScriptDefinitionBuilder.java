package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.Script;

import java.util.regex.Pattern;

public class ScriptDefinitionBuilder implements FunctionDefinitionBuilder<Script> {

    private static final Pattern FUNCTION_NAME_CAPTURE = Pattern.compile("([a-zA-Z_{1}][a-zA-Z0-9_]+)(?=\\()");

    @Override
    public String from(Script script) {
        String body = script.body(); // Script body contains the full function;
        return FUNCTION_NAME_CAPTURE.matcher(body).replaceFirst(script.functionName());
    }
}