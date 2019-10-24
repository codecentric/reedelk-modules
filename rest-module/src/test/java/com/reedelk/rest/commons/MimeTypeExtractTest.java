package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.content.MimeType;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.netty.http.server.HttpServerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MimeTypeExtractTest {

    @Nested
    @DisplayName("From netty http server request")
    class FromNettyHttpServerRequest {

        @Test
        void shouldReturnCorrectMimeTypeFromServerRequest() {
            // Given
            HttpServerRequest request = mock(HttpServerRequest.class);
            HttpHeaders headers = new DefaultHttpHeaders();
            headers.set("content-type", MimeType.APPLICATION_BINARY.toString());

            doReturn(headers).when(request).requestHeaders();

            // When
            MimeType actual = MimeTypeExtract.from(request);

            // Then
            assertThat(actual).isEqualTo(MimeType.APPLICATION_BINARY);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenHeadersDoNotContainContentType() {
            // Given
            HttpServerRequest request = mock(HttpServerRequest.class);
            HttpHeaders headers = new DefaultHttpHeaders();

            doReturn(headers).when(request).requestHeaders();

            // When
            MimeType actual = MimeTypeExtract.from(request);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenServerRequestDoesNotHaveHeaders() {
            // Given
            HttpServerRequest request = mock(HttpServerRequest.class);

            // When
            MimeType actual = MimeTypeExtract.from(request);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("From netty http headers")
    class FromNettyHttpHeaders {

        @Test
        void shouldReturnCorrectMimeTypeWhenContentHeaderIsPresent() {
            // Given
            HttpHeaders headers = new DefaultHttpHeaders();
            headers.set("coNtEnt-Type", MimeType.TEXT.toString());

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.TEXT);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenContentHeaderIsAbsent() {
            // Given
            HttpHeaders headers = new DefaultHttpHeaders();
            headers.set("Content-Length", 234);

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenHeadersAreNull() {
            // Given
            HttpHeaders headers = null;

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("From apache http header")
    class FromApacheHttpHeaders {

        @Test
        void shouldReturnCorrectMimeTypeWhenContentHeaderIsPresent() {
            // Given
            Header[] headers = new Header[1];
            headers[0] = new BasicHeader("conTenT-tyPe", MimeType.APPLICATION_JSON.toString());

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.APPLICATION_JSON);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenContentHeaderIsAbsent() {
            // Given
            Header[] headers = new Header[2];
            headers[0] = new BasicHeader("Content-Length", "123");
            headers[1] = new BasicHeader("Correlation-ID","aabbcc");

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenHeadersAreNull() {
            // Given
            Header[] headers = null;

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }

        @Test
        void shouldReturnUnknownMimeTypeWhenHeadersIsEmpty() {
            // Given
            Header[] headers = new Header[0];

            // When
            MimeType actual = MimeTypeExtract.from(headers);

            // Then
            assertThat(actual).isEqualTo(MimeType.UNKNOWN);
        }
    }
}