package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.script.ScriptSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.script.ScriptException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScriptSourceEvaluatorTest {

    private ScriptSourceEvaluator evaluator;

    @Mock
    private ScriptEngineProvider mockEngineProvider;

    @BeforeEach
    void setUp() {
        evaluator = spy(new ScriptSourceEvaluator());
        doReturn(mockEngineProvider).when(evaluator).scriptEngine();
    }

    @Test
    void shouldCorrectlyCompileScriptSourceModule() throws ScriptException {
        // Given
        Map<String, Object> bindings = new HashMap<>();
        Collection<String> modules = asList("Module1", "Module2");
        StringReader reader = new StringReader("test source code");

        ScriptSource source = mock(ScriptSource.class);
        doReturn(reader).when(source).get();
        doReturn(bindings).when(source).bindings();
        doReturn(modules).when(source).scriptModuleNames();

        // When
        evaluator.register(source);

        // Then
        verify(mockEngineProvider).compile(modules, reader, bindings);
    }
}