package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.DynamicInteger;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JavascriptEngineTest {

    private ScriptEngineService service = JavascriptEngine.INSTANCE;

    @Mock
    private FlowContext context;

    @Test
    void shouldCorrectlyEvaluateMessageAttributeProperty() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        message.getAttributes().put("property1", "test1");
        message.getAttributes().put("property2", "test2");
        DynamicString script = DynamicString.from("#[message.attributes.property1]");

        // When
        Optional<String> attributeProperty = service.evaluate(script, message, context);

        // Then
        assertThat(attributeProperty).isPresent().contains("test1");
    }

    @Test
    void shouldCorrectlyEvaluateNumericValue() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        DynamicInteger script = DynamicInteger.from("#[506]");

        // When
        Optional<Integer> number = service.evaluate(script, message, context);

        // Then
        assertThat(number).isPresent().contains(506);
    }

    @Test
    void shouldCorrectlyEvaluateMapWithScriptAndTextAndNumericValues() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        message.getAttributes().put("property1", "test");

        DynamicMap<String> dynamicMap = DynamicMap.from(of(
                "script", "#[message.attributes.property1]",
                "text", "This is a text",
                "numeric", "23532"));

        // When
        Map<String, String> evaluated = service.evaluate(message, context, dynamicMap);

        // Then
        assertThat(evaluated.get("script")).isEqualTo("test");
        assertThat(evaluated.get("text")).isEqualTo("This is a text");
        assertThat(evaluated.get("numeric")).isEqualTo("23532");
    }

    @Test
    void shouldCorrectlyEvaluateEmptyMap() {
        // Given
        Message message = MessageBuilder.get().empty().build();

        // When
        Map<String, Object> evaluated = service.evaluate(message, context, DynamicMap.empty());

        // Then
        assertThat(evaluated).isEmpty();
    }
}
