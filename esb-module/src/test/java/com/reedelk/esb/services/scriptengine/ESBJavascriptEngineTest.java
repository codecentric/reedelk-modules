package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class ESBJavascriptEngineTest {

    private ScriptEngineService service = ESBJavascriptEngine.INSTANCE;

    @Test
    void shouldCorrectlyEvaluateMessageInboundProperty() throws ScriptException {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        message.getAttributes().put("property1", "test");
        String script = "message.attributes.property1";

        // When
        String property = service.evaluate(message, script);

        // Then
        assertThat(property).isNotNull();
        assertThat(property).isEqualTo("test");
    }
}
