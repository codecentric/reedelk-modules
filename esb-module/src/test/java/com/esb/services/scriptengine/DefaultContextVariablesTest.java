package com.esb.services.scriptengine;

import com.esb.api.message.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultContextVariablesTest {

    @Test
    void shouldBindMessageToCorrectMessage() {
        // Given
        Message message = new Message();

        // When
        DefaultContextVariables context = new DefaultContextVariables(message);

        // Then
        assertThat(context.get("message")).isEqualTo(message);
    }

    @Test
    void shouldBindInboundPropertiesToCorrectProperties() {
        // Given
        Message message = new Message();

        // When
        DefaultContextVariables context = new DefaultContextVariables(message);

        // Then
        assertThat(context.get("inboundProperties")).isEqualTo(message.getInboundProperties());
    }

    @Test
    void shouldBindOutboundPropertiesToCorrectProperties() {
        // Given
        Message message = new Message();

        // When
        DefaultContextVariables context = new DefaultContextVariables(message);

        // Then
        assertThat(context.get("outboundProperties")).isEqualTo(message.getOutboundProperties());
    }
}