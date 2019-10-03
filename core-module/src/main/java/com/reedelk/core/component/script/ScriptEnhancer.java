package com.reedelk.core.component.script;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptBlock;

public class ScriptEnhancer implements ScriptBlock {

    private static final String EXECUTION_SCRIPT_TEMPLATE =
            "var input = JSON.parse(message.payload());\n" +
            "var output = {};\n" + // TODO: EMPTY object is just {}
            "%s\n" +
            "return JSON.stringify(output);";


    private final Script delegate;
    private final String realBody;

    ScriptEnhancer(Script delegate) {
        this.delegate = delegate;
        String userDefined = ScriptUtils.unwrap(delegate.body());
        this.realBody = String.format(EXECUTION_SCRIPT_TEMPLATE, userDefined);
    }

    @Override
    public String uuid() {
        return delegate.uuid();
    }

    @Override
    public String body() {
        return realBody;
    }

    @Override
    public boolean isEmpty() {
        return ScriptUtils.isEmpty(realBody);
    }
}
