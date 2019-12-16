package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptBlockContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

class ScriptDefinitionBuilderTest {

    private ScriptDefinitionBuilder builder;
    private ScriptBlockContext context = new ScriptBlockContext(10L);

    @BeforeEach
    void setUp() {
        builder = new ScriptDefinitionBuilder();
    }

    @Test
    void shouldCorrectlyReplaceOriginalFunctionNameWithGeneratedFunctionName() {
        // Given
        String myFunction = "function myFunction(message,context) {\n" +
                "   return 'This is a test';\n" +
                "}\n";

        String expectedReplaced = "function %s(message,context) {\n" +
                "   return 'This is a test';\n" +
                "}\n";

        Script script = Script.from(myFunction, context);

        // When
        String replaced = builder.from(script);

        // Then
        String randomlyGeneratedFunctionName = script.functionName();
        assertThat(replaced).isEqualTo(format(expectedReplaced, randomlyGeneratedFunctionName));
    }
}