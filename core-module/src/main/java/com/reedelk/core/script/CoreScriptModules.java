package com.reedelk.core.script;

import com.reedelk.core.component.logger.ScriptLogger;
import com.reedelk.runtime.api.annotation.AutocompleteContributor;
import com.reedelk.runtime.api.script.ScriptSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@AutocompleteContributor(contributions = {
        "Util[VARIABLE:Util]",
        "Util.tmpdir()[FUNCTION:String]",
        "Util.uuid()[FUNCTION:String]",
        "Log[VARIABLE:Log]",
        "Log.info()[FUNCTION:void]",
        "Log.debug()[FUNCTION:void]",
        "Log.warn()[FUNCTION:void]",
        "Log.error()[FUNCTION:void]",
        "Log.trace()[FUNCTION:void]"
})
public class CoreScriptModules implements ScriptSource {

    private final long moduleId;

    public CoreScriptModules(long moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public Map<String, Object> bindings() {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("logger", new ScriptLogger());
        return bindings;
    }

    @Override
    public Collection<String> scriptModuleNames() {
        return unmodifiableList(asList("Util", "Log"));
    }

    @Override
    public long moduleId() {
        return moduleId;
    }

    @Override
    public String resource() {
        return "/function/core-javascript-functions.js";
    }
}