package com.esb.services.scriptengine;

import com.esb.api.message.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextVariablesTest {

    @Test
    void shouldBindPayloadToNullWhenMessageDoesNotHaveContent() {
        // Given
        Message message = new Message();

        // When
        ContextVariables context = new ContextVariables(message);

        // Then
        assertThat(context.get("payload")).isNull();
    }

    @Test
    void shouldBindPayloadToMessageContent() {
        // Given
        Type type = new Type(MimeType.TEXT, String.class);
        TypedContent<String> content = new MemoryTypedContent<>("Test", type);

        Message message = new Message();
        message.setTypedContent(content);

        // When
        ContextVariables context = new ContextVariables(message);

        // Then
        assertThat(context.get("payload")).isEqualTo("Test");
    }

    @Test
    void shouldBindMessageToCorrectMessage() {
        // Given
        Message message = new Message();

        // When
        ContextVariables context = new ContextVariables(message);

        // Then
        assertThat(context.get("message")).isEqualTo(message);
    }

    @Test
    void shouldBindInboundPropertiesToCorrectProperties() {
        // Given
        Message message = new Message();

        // When
        ContextVariables context = new ContextVariables(message);

        // Then
        assertThat(context.get("inboundProperties")).isEqualTo(message.getInboundProperties());
    }

    @Test
    void shouldBindOutboundPropertiesToCorrectProperties() {
        // Given
        Message message = new Message();

        // When
        ContextVariables context = new ContextVariables(message);

        // Then
        assertThat(context.get("outboundProperties")).isEqualTo(message.getOutboundProperties());
    }
}