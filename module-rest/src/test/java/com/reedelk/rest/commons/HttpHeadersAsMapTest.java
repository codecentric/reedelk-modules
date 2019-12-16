package com.reedelk.rest.commons;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class HttpHeadersAsMapTest {

    @Nested
    @DisplayName("Map from netty headers")
    class OfNettyHeaders {

        @Test
        void shouldMapAllHeaders() {
            // Given
            HttpHeaders headers = new DefaultHttpHeaders();
            headers.set("header1", "value1");
            headers.set("header2", "value1,value2");

            // When
            TreeMap<String, List<String>> headersMap = HttpHeadersAsMap.of(headers);

            // Then
            assertThat(headersMap).containsEntry("header1", singletonList("value1"));
            assertThat(headersMap).containsEntry("header2", asList("value1", "value2"));
        }

        @Test
        void shouldReturnEmptyWhenInputIsNull() {
            // Given
            HttpHeaders headers = null;

            // When
            TreeMap<String, List<String>> headersMap = HttpHeadersAsMap.of(headers);

            // Then
            assertThat(headersMap).isEmpty();
        }
    }

    @Nested
    @DisplayName("Map from apache headers")
    class OfApacheHeaders {

        @Test
        void shouldMapAllHeaders() {
            // Given
            Header[] headers = new Header[2];
            headers[0] = new BasicHeader("header1", "value1,value2");
            headers[1] = new BasicHeader("header2", "valueX");

            // When
            TreeMap<String, List<String>> headersMap = HttpHeadersAsMap.of(headers);

            // Then
            assertThat(headersMap).containsEntry("header1", asList("value1", "value2"));
            assertThat(headersMap).containsEntry("header2", singletonList("valueX"));
        }

        @Test
        void shouldReturnEmptyWhenInputIsNull() {
            // Given
            Header[] headers = null;

            // When
            TreeMap<String, List<String>> headersMap = HttpHeadersAsMap.of(headers);

            // Then
            assertThat(headersMap).isEmpty();
        }
    }
}