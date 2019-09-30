package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JavascriptEngineTest {

    private ScriptEngineService service = JavascriptEngine.INSTANCE;

    @Mock
    private FlowContext context;


    @Nested
    @DisplayName("Evaluate dynamic map with message and context")
    class EvaluateDynamicMapWithMessageAndContext {

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
            Map<String, String> evaluated = service.evaluate(dynamicMap, message, context);

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
            Map<String, Object> evaluated = service.evaluate(DynamicMap.empty(), message, context);

            // Then
            assertThat(evaluated).isEmpty();
        }
    }
}
