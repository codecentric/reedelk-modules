package com.reedelk.esb.services.scriptengine;

import org.junit.jupiter.api.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

class NativeJavascriptEngineTest {


    @Test
    void shouldEvaluateMap() throws ScriptException, NoSuchMethodException {
        // Given
        ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
        String script = "var mapFunction = function(arg) {" +
                "return {name: arg.getValue()}" +
                "}";
        nashorn.eval(script);
        MyObjectArg arg = new MyObjectArg();
        arg.setValue("hello spenk");

        // When
        Object result = ((Invocable) nashorn).invokeFunction("mapFunction", arg);


        // Then
        System.out.println(result);
    }

}
