package com.esb.services.scriptengine;

import com.esb.api.message.MemoryTypedContent;
import com.esb.api.message.Message;
import com.esb.api.message.MimeType;
import com.esb.api.message.Type;
import com.esb.api.service.ScriptEngineService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

public class ESBJavascriptEngineTest {

    private ScriptEngineService service = ESBJavascriptEngine.INSTANCE;

    @Test
    void shouldDoSomething() throws ScriptException {
        // Given
        Message message = new Message();
        Type contentType = new Type(MimeType.TEXT, String.class);
        MemoryTypedContent<String> hello = new MemoryTypedContent<>("{}", contentType);
        message.setTypedContent(hello);
        message.getInboundProperties().setProperty("property1", "test");
        String script = "message.inboundProperties.property1";

        // When
        String property = service.evaluate(message, script, String.class);

        // Then
        Assertions.assertThat(property).isNotNull();
    }

}
