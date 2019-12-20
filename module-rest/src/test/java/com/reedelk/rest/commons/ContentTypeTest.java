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
        Message messageWithJson = MessageBuilder.get().withJson("{}").build();

        // When
        Optional<String> maybeContentType = ContentType.from(messageWithJson);

        // Then
        assertThat(maybeContentType).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenMessageHasNullContent() {
        // Given
        Message messageWithNullContent = MessageBuilder.get().empty().build();

        // When
        Optional<String> maybeContentType = ContentType.from(messageWithNullContent);

        // Then
        assertThat(maybeContentType).isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenMessageContentHasNullMimeType() {
        // Given
        TypedContent<?> content = new StringContent("test", null);
        Message message = MessageBuilder.get().typedContent(content).build();

        // When
        Optional<String> maybeContentType = ContentType.from(message);

        // Then
        assertThat(maybeContentType).isNotPresent();
    }
}