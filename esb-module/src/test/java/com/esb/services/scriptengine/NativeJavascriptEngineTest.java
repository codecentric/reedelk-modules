package com.esb.services.scriptengine;

import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class NativeJavascriptEngineTest {

    @Test
    void shouldCorrectlyUpdateBindingVariablesAfterExecution() throws ScriptException {
        // Given
        ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
        Bindings bindings = nashorn.createBindings();
        bindings.put("input", "{\"firstName\":\"Mark\"}");
        bindings.put("output", "{}");
        // When
        Object eval = nashorn.eval("" +
                "input = JSON.parse(input); " +
                "output = JSON.parse('{}'); " +
                "output.firstName = input.firstName + ' test suffix'; " +
                "output = JSON.stringify(output);", bindings);

        // Then
        Object output = bindings.get("output");
        assertThat(output).isEqualTo("{\"firstName\":\"Mark test suffix\"}");
    }
}
