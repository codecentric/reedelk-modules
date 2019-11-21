package com.reedelk.esb.services.scriptengine;

import com.reedelk.esb.exception.ScriptCompilationException;
import com.reedelk.esb.services.scriptengine.evaluator.ScriptEngineProvider;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;

import static com.reedelk.esb.commons.Messages.Script.SCRIPT_COMPILATION_ERROR;
import static com.reedelk.esb.commons.Messages.Script.SCRIPT_COMPILATION_ERROR_WITH_FUNCTION;
import static javax.script.ScriptContext.ENGINE_SCOPE;

public class JavascriptEngineProvider implements ScriptEngineProvider {

    private final NashornScriptEngine engine;

    private JavascriptEngineProvider() {
        this.engine = (NashornScriptEngine) new NashornScriptEngineFactory()
                .getScriptEngine(new String[]{"--optimistic-types=false"});
    }

    private static class ScriptEngineProviderHelper {
        private static final JavascriptEngineProvider INSTANCE = new JavascriptEngineProvider();
    }

    public static JavascriptEngineProvider getInstance() {
        return ScriptEngineProviderHelper.INSTANCE;
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
    public void eval(String functionDefinition) {
        try {

            CompiledScript compiled = engine.compile(functionDefinition);

            compiled.eval(engine.getBindings(ENGINE_SCOPE));

        } catch (ScriptException exception) {
            String errorMessage = SCRIPT_COMPILATION_ERROR_WITH_FUNCTION.format(functionDefinition, exception.getMessage());
            throw new ScriptCompilationException(errorMessage, exception);
        }
    }

    @Override
    public void eval(Collection<String> moduleNames, Reader reader, Map<String, Object> customBindings) {
        try {

            // We create a temporary binding object just to pass custom initialization bindings
            // to the module. They are not needed in the engine scope, but just for module
            // initialization.

            Bindings tmpBindings = engine.createBindings();

            tmpBindings.putAll(customBindings);

            CompiledScript compiled = engine.compile(reader);

            compiled.eval(tmpBindings);

            Bindings bindings = engine.getBindings(ENGINE_SCOPE);

            moduleNames.forEach(moduleName -> bindings.put(moduleName, tmpBindings.get(moduleName)));

        } catch (ScriptException exception) {
            String errorMessage = SCRIPT_COMPILATION_ERROR.format(exception.getMessage());
            throw new ScriptCompilationException(errorMessage, exception);
        }
    }

    @Override
    public void removeModule(String moduleName) {
        engine.getBindings(ENGINE_SCOPE).remove(moduleName);
    }

    /**
     * We free-up references to the function by setting the value to null.
     * Functions are therefore not removed. We could create and set a new bindings object
     * for the ENGINE_SCOPE, removing completely those functions, however it would be
     * disruptive for running Script Functions. Since all dynamic values have a
     * unique UUID, we just set the current value to null to free-up the space associated with
     * the key. The key will be kept inside the bindings and it will have just null value.
     * ----------------------------------------------------------------------
     * An approach to cleanup the bindings could be:
     * Bindings newBindings = engine.createBindings();
     * engine.getBindings(ENGINE_SCOPE).forEach((key, value) -> {
     *    if (!key.equals(functionName)) {
     *        newBindings.put(key, value);
     *    }
     * });
     * engine.setBindings(newBindings, ENGINE_SCOPE);
     * However this approach would require synchronization of the current script execution
     * which would slow down performances. By keeping function references to null we are
     * making a trade-off between execution speed and memory consumption.
     * In the future one strategy could be scheduling a scope cleanup when the functions
     * with null references are greater than a given number for instance.
     * The current approach (setting the value to null) make sense in this case because
     * functions names are randomly generated with a UUID, therefore they will never be
     * called anymore after a module has been un-installed.
     * // ----------------------------------------------------------------------
     *
     * @param functionName the name of the function to be cleaned up (set to null)
     */
    @Override
    public void removeFunction(String functionName) {
        engine.getBindings(ENGINE_SCOPE).put(functionName, null);
    }
}
