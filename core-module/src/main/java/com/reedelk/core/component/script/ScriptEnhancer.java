package com.reedelk.core.component.script;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.Script;

// TODO: Create a component using this approach.
class ScriptEnhancer extends Script {

    private static final String EXECUTION_SCRIPT_TEMPLATE =
            "var input = JSON.parse(message.payload());\n" +
            "var output = {};\n" +
            "%s\n" +
            "return JSON.stringify(output);";

    private ScriptEnhancer(Script script, String body) {
        super(script, body);
    }

    static Script enhance(Script original) {
        String userDefined = ScriptUtils.unwrap(original.body());
        String realBody = String.format(EXECUTION_SCRIPT_TEMPLATE, userDefined);
        return new ScriptEnhancer(original, realBody);
    }
}
