package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@SuppressWarnings("unchecked")
public enum JavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(JavascriptEngine.class);

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;
    private Invocable invocable;

    JavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        invocable = (Invocable) engine;
    }

    @Override
    public <T> T evaluate(DynamicValue value, Throwable exception, FlowContext flowContext) {
        if (value.isScript()) {
            String functionName = functionNameOf(value, INLINE_ERROR_SCRIPT);
            try {
                return (T) invocable.invokeFunction(functionName, exception, flowContext);
            } catch (ScriptException | NoSuchMethodException e) {
                throw new ESBException(e);
            }
        } else {
            return (T) value.getBody();
        }
    }

    @Override
    public <T> T evaluate(DynamicValue value, Message message, FlowContext flowContext) {
        if (value.isScript()) {
            String functionName = functionNameOf(value, INLINE_SCRIPT);
            try {
                return (T) invocable.invokeFunction(functionName, message,  message.getContent().data(), flowContext);
            } catch (ScriptException | NoSuchMethodException e) {
                throw new ESBException(e);
            }
        } else {
            return (T) value.getBody();
        }
    }

    @Override
    public <T> T evaluate(Script script, Message message, FlowContext flowContext) {
        throw new IllegalArgumentException("Implement em");
    }

    private static final Map<String,?> EMPTY_MAP = Collections.unmodifiableMap(Collections.emptyMap());

    @Override
    public <T> Map<String, T> evaluate(Message message, FlowContext context, DynamicMap<T> dyamicMap) {
        if (dyamicMap.isEmpty()) {
            // If dynamic map is empty, nothing to do.
            return (Map<String, T>) EMPTY_MAP;
        } else {
            String functionName = functionNameOf(dyamicMap);
            try {
                return (Map<String, T>) invocable.invokeFunction(functionName, message, message.getContent().data(), context);
            } catch (ScriptException | NoSuchMethodException e) {
                throw new ESBException(e);
            }
        }
    }


    @Override
    public void onDisposed(Component component) {
        // TODO: Complete me
        //String key = key(component);
        //ORIGIN_FUNCTION_NAME.remove(key);
    }


    private static final String INLINE_SCRIPT =
            "var %s = function(message, payload, context) {\n" +
                    "  return %s\n" +
                    "};";

    private static final String INLINE_ERROR_SCRIPT =
            "var %s = function(error, context) {\n" +
                    "  return %s\n" +
                    "};";



    private final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();


    /**
     * We compile the function body if a function has not been registered yet.
     * @param dynamicValue the dynamic value.
     * @return the function name required to evaluate this dynamic value.
     */
    private String functionNameOf(DynamicValue dynamicValue, String template) {
        String valueUUID =  dynamicValue.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    String functionDefinition = format(template, functionName, ScriptUtils.unwrap(dynamicValue.getBody()));
                    try {
                        engine.eval(functionDefinition);
                        ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                    } catch (ScriptException e) {
                        throw new ESBException(e);
                    }
                }
            }
        }
        return functionName;
    }

    private <T> String functionNameOf(DynamicMap<T> dynamicMap) {
        String valueUUID =  dynamicMap.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    EvaluateMapFunction<T> evaluateMapFunction = new EvaluateMapFunction<>(functionName, dynamicMap);
                    String functionDefinition = evaluateMapFunction.script();
                    try {
                        engine.eval(functionDefinition);
                        ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                    } catch (ScriptException e) {
                        throw new ESBException(e);
                    }
                }
            }
        }
        return functionName;
    }
}
