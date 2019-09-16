package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ESBJavascriptEngineTest {

    private ScriptEngineService service = ESBJavascriptEngine.INSTANCE;

    @Mock
    private FlowContext context;

    @Test
    void shouldCorrectlyEvaluateMessageAttributeProperty() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        message.getAttributes().put("property1", "test1");
        message.getAttributes().put("property2", "test2");
        String script = "message.attributes.property1";

        // When
        String attributeProperty = service.evaluate(script, message);

        // Then
        assertThat(attributeProperty).isEqualTo("test1");
    }

    @Test
    void shouldCorrectlyEvaluateMapWithScriptAndTextAndNumericValues() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        message.getAttributes().put("property1", "test");

        // When
        NMapEvaluation maps = service.evaluate(
                message,
                context,
                ImmutableMap.of(
                        "script", "#[message.attributes.property1]",
                        "text", "This is a text",
                        "numeric", 23532));

        // Then
        Map evaluated = maps.map(0);
        assertThat(evaluated.get("script")).isEqualTo("test");
        assertThat(evaluated.get("text")).isEqualTo("This is a text");
        assertThat(evaluated.get("numeric")).isEqualTo(23532);
    }

    @Test
    void shouldCorrectlyEvaluateMultipleMaps() {
        // Given
        Message message = MessageBuilder.get().text("test").build();

        // When
        NMapEvaluation<Object> maps = service.evaluate(
                message,
                context,
                ImmutableMap.of(
                        "text", "This is a text",
                        "numeric", 23532),
                ImmutableMap.of(
                        "script", "#[payload]",
                        "text", "second map"));

        // Then
        Map<String,Object> evaluated0 = maps.map(0);
        assertThat(evaluated0.get("text")).isEqualTo("This is a text");
        assertThat(evaluated0.get("numeric")).isEqualTo(23532);

        Map<String,Object> evaluated1 = maps.map(1);
        assertThat(evaluated1.get("script")).isEqualTo("test");
        assertThat(evaluated1.get("text")).isEqualTo("second map");
    }

    @Test
    void shouldCorrectlyEvaluateEmptyMap() {
        // Given
        Message message = MessageBuilder.get().empty().build();

        // When
        NMapEvaluation<Object> maps = service.evaluate(
                message,
                context,
                ImmutableMap.of());

        // Then
        Map<String,Object> evaluated0 = maps.map(0);
        assertThat(evaluated0).isEmpty();
    }
}
