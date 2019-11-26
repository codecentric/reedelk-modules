package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.exception.ScriptCompilationException;
import com.reedelk.esb.exception.ScriptExecutionException;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.script.ScriptBlockContext;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.script.ScriptException;

import static com.reedelk.esb.pubsub.Action.Module.ActionModuleUninstalled;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This test verifies the behaviour of the invokeFunction: make sure that
 * a function is compiled if it does not exists yet. Moreover, this test
 * verifies that when 'onModuleUninstalled' event is fired, previously
 * defined functions are correctly removed from the script engine.
 */
@ExtendWith(MockitoExtension.class)
class AbstractDynamicValueEvaluatorTest {

    private final long testModuleId = 10L;
    private ScriptBlockContext scriptBlockContext = new ScriptBlockContext(testModuleId);

    @Mock
    private ScriptEngineProvider mockEngineProvider;

    private TestAwareAbstractDynamicValueEvaluatorTest evaluator;
    private TestFunctionBuilder testFunctionBuilder = new TestFunctionBuilder();

    @BeforeEach
    void setUp() {
        evaluator = spy(new TestAwareAbstractDynamicValueEvaluatorTest());
        doReturn(mockEngineProvider).when(evaluator).scriptEngine();
    }

    @Test
    void shouldCompileScriptWhenFunctionIsNotFoundAndInvokeAgainFunctionAfterCompilation() throws NoSuchMethodException, ScriptException {
        // Given
        String expectedResult = "evaluation result";
        DynamicString dynamicValue = DynamicString.from("#['evaluation result']", scriptBlockContext);

        when(mockEngineProvider
                .invokeFunction(dynamicValue.functionName()))
                .thenThrow(new NoSuchMethodException("method not found"))
                .thenReturn(expectedResult);

        // When
        Object actualResult = evaluator.invokeFunction(dynamicValue, testFunctionBuilder);

        // Then
        verify(mockEngineProvider).compile(anyString());
        verify(mockEngineProvider, times(2)).invokeFunction(dynamicValue.functionName());
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenScriptCouldNotBeCompiled() throws NoSuchMethodException, ScriptException {
        // Given
        DynamicString dynamicValue = DynamicString.from("#[notValid'Script']", scriptBlockContext);

        when(mockEngineProvider
                .invokeFunction(dynamicValue.functionName()))
                .thenThrow(new NoSuchMethodException("method not found"));

        doThrow(new ScriptException("expected ' ' but 'S was found"))
                .when(mockEngineProvider)
                .compile(anyString());

        // When
        ScriptCompilationException thrown = assertThrows(ScriptCompilationException.class,
                () -> evaluator.invokeFunction(dynamicValue, testFunctionBuilder));

        // Then

        verify(mockEngineProvider).invokeFunction(anyString());
        verify(mockEngineProvider).compile(anyString());
        verifyNoMoreInteractions(mockEngineProvider);
        assertThat(thrown).hasMessage("Could not compile script: expected ' ' but 'S was found,\n" +
                "- Script code:\n" +
                "#[notValid'Script']");
    }

    @Test
    void shouldRethrowExceptionWithScriptBodyWhenScriptExceptionIsThrown() throws NoSuchMethodException, ScriptException {
        // Given
        DynamicString dynamicValue = DynamicString.from("#['test' + unknownVariable]", scriptBlockContext);

        doThrow(new ScriptException("variable not found unknownVariable"))
                .when(mockEngineProvider)
                .invokeFunction(dynamicValue.functionName());

        // When
        ScriptExecutionException exception = assertThrows(ScriptExecutionException.class,
                () -> evaluator.invokeFunction(dynamicValue, testFunctionBuilder));

        // Then
        assertThat(exception).hasMessage("Could not execute script: variable not found unknownVariable,\n" +
                "- Script code:\n" +
                "#['test' + unknownVariable]");
    }

    @Test
    void shouldCompileRegisterFunctionsCorrectlyForModuleId() throws ScriptException {
        // Given
        DynamicString dynamicValue1 = DynamicString.from("#['evaluation result']", scriptBlockContext);
        DynamicString dynamicValue2 = DynamicString.from("#['another evaluation result']", scriptBlockContext);

        // When
        evaluator.compile(dynamicValue1, testFunctionBuilder);
        evaluator.compile(dynamicValue2, testFunctionBuilder);

        // Then
        verify(mockEngineProvider).compile(testFunctionBuilder.from(dynamicValue1));
        verify(mockEngineProvider).compile(testFunctionBuilder.from(dynamicValue2));
        assertThat(evaluator.moduleIdFunctionNamesMap)
                .containsEntry(
                        scriptBlockContext.getModuleId(),
                        asList(dynamicValue1.functionName(), dynamicValue2.functionName()));
    }

    @Test
    void shouldRemoveEntryFromModuleIdFunctionMapWhenModuleUninstalled() {
        // Given
        DynamicString dynamicValue1 = DynamicString.from("#['evaluation result']", scriptBlockContext);
        DynamicString dynamicValue2 = DynamicString.from("#['another evaluation result']", scriptBlockContext);

        evaluator.compile(dynamicValue1, testFunctionBuilder);
        evaluator.compile(dynamicValue2, testFunctionBuilder);

        // When
        ActionModuleUninstalled actionModuleUninstalled = new ActionModuleUninstalled(testModuleId);
        evaluator.onModuleUninstalled(actionModuleUninstalled);

        // Then
        assertThat(evaluator.moduleIdFunctionNamesMap).doesNotContainKey(testModuleId);
    }

    @Test
    void shouldUndefineFunctionFromScriptEngineWhenModuleUninstalled() throws ScriptException {
        // Given
        DynamicString dynamicValue1 = DynamicString.from("#['evaluation result']", scriptBlockContext);
        DynamicString dynamicValue2 = DynamicString.from("#['another evaluation result']", scriptBlockContext);

        evaluator.compile(dynamicValue1, testFunctionBuilder);
        evaluator.compile(dynamicValue2, testFunctionBuilder);

        // When
        ActionModuleUninstalled actionModuleUninstalled = new ActionModuleUninstalled(testModuleId);
        evaluator.onModuleUninstalled(actionModuleUninstalled);

        // Then
        verify(mockEngineProvider).undefineFunction(dynamicValue1.functionName());
        verify(mockEngineProvider).undefineFunction(dynamicValue2.functionName());
        verify(mockEngineProvider, times(2)).compile(anyString());
        verifyNoMoreInteractions(mockEngineProvider);
    }

    private class TestAwareAbstractDynamicValueEvaluatorTest extends AbstractDynamicValueEvaluator {
    }

    class TestFunctionBuilder implements FunctionDefinitionBuilder<DynamicValue> {

        private static final String TEMPLATE =
                "function %s() {\n" +
                        "%s\n" +
                        "};";

        @Override
        public String from(DynamicValue dynamicValue) {
            return format(TEMPLATE, dynamicValue.functionName(), dynamicValue.body());
        }
    }
}
