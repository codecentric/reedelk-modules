package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.StringContent;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
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
    void shouldReturnEmptyWhenMessageContentHasNullType() {
        // Given
        Type type = null;
        TypedContent<?> contentWithNullType = new StringContent("test", type);
        Message message = new Message();
        message.setContent(contentWithNullType);

        // When
        Optional<String> maybeContentType = ContentType.from(message);

        // Then
        assertThat(maybeContentType).isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenMessageContentHasNullMimeType() {
        // Given
        Type typeWithNullMimeType = new Type(null);
        TypedContent<?> content = new StringContent("test", typeWithNullMimeType);
        Message message = new Message();
        message.setContent(content);

        // When
        Optional<String> maybeContentType = ContentType.from(message);

        // Then
        assertThat(maybeContentType).isNotPresent();
    }
}