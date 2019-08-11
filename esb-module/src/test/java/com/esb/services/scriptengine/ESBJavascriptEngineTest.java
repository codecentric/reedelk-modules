package com.esb.services.scriptengine;

import com.esb.api.service.ScriptEngineService;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

class ESBJavascriptEngineTest {

    private ScriptEngineService service = ESBJavascriptEngine.INSTANCE;

    @Test
    void shouldCorrectlyEvaluateMessageInboundProperty() throws ScriptException {
        // Given
        //TODO: Use message builder
        /**
         Message message = new Message();
         Type contentType = new Type(MimeType.TEXT, String.class);
         MemoryTypedContent<String> hello = new MemoryTypedContent<>("{}", contentType);
         message.setTypedContent(hello);
         message.getInboundProperties().setProperty("property1", "test");
         String script = "message.inboundProperties.property1";

         // When
         String property = service.evaluate(message, script, String.class);

         // Then
         assertThat(property).isNotNull();
         assertThat(property).isEqualTo("test");
         */
    }

}
