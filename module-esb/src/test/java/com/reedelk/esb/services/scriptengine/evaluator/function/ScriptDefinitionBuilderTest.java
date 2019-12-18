package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.commons.ModuleId;
import com.reedelk.runtime.api.script.Script;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

class ScriptDefinitionBuilderTest {

    private final ModuleId moduleId = new ModuleId(10L);
    private final ModuleContext context = new ModuleContext(moduleId);

    private ScriptDefinitionBuilder builder;

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