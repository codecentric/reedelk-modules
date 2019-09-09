package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFlowContextVariablesTest {

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
        assertThat(context.get("attributes")).isEqualTo(message.getAttributes());
    }
}