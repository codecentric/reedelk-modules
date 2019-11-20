package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.exception.ScriptCompilationException;
import com.reedelk.esb.services.scriptengine.evaluator.ScriptEngineProvider;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Map;

import static com.reedelk.esb.commons.Messages.Script.SCRIPT_COMPILATION_ERROR;
import static com.reedelk.esb.commons.Messages.Script.SCRIPT_COMPILATION_ERROR_WITH_FUNCTION;
import static javax.script.ScriptContext.ENGINE_SCOPE;

public class JavascriptEngineProvider implements ScriptEngineProvider {

    public static final ScriptEngineProvider INSTANCE = new JavascriptEngineProvider();

    private static final String ENGINE_NAME = "nashorn";

    private final NashornScriptEngine engine;
    private final Bindings bindings;

    private JavascriptEngineProvider() {
        this.engine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        this.bindings = engine.getBindings(ENGINE_SCOPE);
    }

    @Override
    public Object invokeFunction(String functionName, Object... args) {
        try {
            return engine.invokeFunction(functionName, args);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eval(String moduleName, Reader reader, Map<String, Object> customBindings) {
        try {

            // We create a temporary binding object just to pass custom initialization bindings
            // to the module. They are not needed in the engine scope, but just for module
            // initialization.

            Bindings tmpBindings = engine.createBindings();

            tmpBindings.putAll(customBindings);

            CompiledScript compiled = engine.compile(reader);

            compiled.eval(tmpBindings);

            Object moduleObject = tmpBindings.get(moduleName);

            bindings.put(moduleName, moduleObject);

        } catch (ScriptException exception) {
            String errorMessage = SCRIPT_COMPILATION_ERROR.format(exception.getMessage());
            throw new ScriptCompilationException(errorMessage, exception);
        }
    }

    @Override
    public void eval(String functionDefinition) {
        try {

            CompiledScript compiled = engine.compile(functionDefinition);

            compiled.eval(bindings);

        } catch (ScriptException exception) {
            String errorMessage = SCRIPT_COMPILATION_ERROR_WITH_FUNCTION.format(functionDefinition, exception.getMessage());
            throw new ScriptCompilationException(errorMessage, exception);
        }
    }

    @Override
    public void clear(String module) {
        bindings.put(module, null);
    }
}
