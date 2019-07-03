package com.esb.services.scriptengine;

import com.esb.api.message.Message;
import com.esb.api.service.ScriptEngineService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public enum ESBJavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final String ENGINE_NAME = "nashorn";

    private static final String JSON_TYPE_PAYLOAD = "var payload = JSON.parse(message.typedContent.content);\n";

    private final ScriptEngine engine;

    ESBJavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
    }

    @Override
    public <T> T evaluate(Message message, String script, Class<T> returnType) throws ScriptException {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("message", message);
        Object result = engine.eval(JSON_TYPE_PAYLOAD + script, bindings);
        return (T) convert(result, returnType);
    }

    private <T> Object convert(Object result, Class<T> returnType) {
        if (result instanceof Boolean && returnType == Boolean.class) return result;
        if (result instanceof String && returnType == Boolean.class) {
            return Boolean.parseBoolean((String) result);
        } else {
            return result;
        }
        //throw new ESBException("Could not convert");
    }
}
