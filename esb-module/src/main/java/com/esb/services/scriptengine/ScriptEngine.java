package com.esb.services.scriptengine;

import com.esb.api.message.Message;

import javax.script.ScriptException;

public interface ScriptEngine {

    <T> T evaluate(Message message, String script, Class<T> returnType) throws ScriptException;

}
