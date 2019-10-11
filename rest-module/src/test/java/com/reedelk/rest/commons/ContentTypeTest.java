package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeTest {

    @Test
    void shouldReturnCorrectContentType() {
        // Given
        Message messageWithJson = MessageBuilder.get().json("{}").build();

        // When
        Optional<String> maybeContentType = ContentType.from(messageWithJson);

        // Then
        assertThat(maybeContentType).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenMessageHasNullContent() {
        // Given
        Message messageWithNullContent = new Message();

        // When
        Optional<String> maybeContentType = ContentType.from(messageWithNullContent);

        // Then
        assertThat(maybeContentType).isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenMessageContentHasNullMimeType() {
        // Given
        TypedContent<?> content = new StringContent("test", null);
        Message message = new Message();
        message.setContent(content);

        // When
        Optional<String> maybeContentType = ContentType.from(message);

        // Then
        assertThat(maybeContentType).isNotPresent();
    }
}