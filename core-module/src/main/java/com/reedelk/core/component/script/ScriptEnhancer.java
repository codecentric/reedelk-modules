package com.reedelk.core.component.script;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptBlock;

public class ScriptEnhancer implements ScriptBlock {

    private static final String EXECUTION_SCRIPT_TEMPLATE =
            "var input = JSON.parse(message.payload());\n" +
            "var output = JSON.parse('{}');\n" + // TODO: EMPTY object is just {}
            "%s\n" +
            "return JSON.stringify(output);";


    private final Script delegate;

    ScriptEnhancer(Script delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getUUID() {
        return delegate.getUUID();
    }

    @Override
    public String getScriptBody() {
        String userDefined = ScriptUtils.unwrap(delegate.getScriptBody());
        return String.format(EXECUTION_SCRIPT_TEMPLATE, userDefined);
    }

    @Override
    public boolean isEmptyScript() {
        // TODO: Not sure if this is right...
        return ScriptUtils.isEmpty(delegate.getScriptBody());
    }
}
