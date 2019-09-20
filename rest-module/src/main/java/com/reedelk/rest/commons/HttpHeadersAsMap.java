package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.Header;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class HttpHeadersAsMap {

    public static HeadersMap of(HttpHeaders headers) {
        HeadersMap headersMap = new HeadersMap();
        if (headers != null) {
            headers.names().forEach(headerName -> {
                String headerValue = headers.get(headerName);
                headersMap.put(headerName, splitHeaderValue(headerValue));
            });
        }
        return headersMap;
    }

    public static HeadersMap of(Header[] headers) {
        HeadersMap headersMap = new HeadersMap();
        if (headers != null) {
            Stream.of(headers).forEach(header -> {
                String headerName = header.getName();
                String headerValue = header.getValue();
                headersMap.put(headerName, splitHeaderValue(headerValue));
            });
        }
        return headersMap;
    }

    /**
     * HTTP spec RFC 2616 says:
     * Multiple message-header fields with the same field-name MAY be present in a message if and only if the
     * entire field-value for that header field is defined as a comma-separated list [i.e., #(values)].
     * It MUST be possible to combine the multiple header fields into one "field-name: field-value" pair,
     * without changing the semantics of the message, by appending each subsequent field-value to the first,
     * each separated by a comma. The order in which header fields with the same field-name are received is
     * therefore significant to the interpretation of the combined field value, and thus a proxy MUST NOT
     * change the order of these field values when a message is forwarded.
     *
     * @param commaSeparatedHeaderValue header value potentially with comma separated values.
     * @return a list containing the header values previously separated by a comma.
     */
    private static List<String> splitHeaderValue(String commaSeparatedHeaderValue) {
        if (StringUtils.isBlank(commaSeparatedHeaderValue)) return Collections.emptyList();
        return stream(commaSeparatedHeaderValue.split(","))
                .map(StringUtils::trim)
                .collect(toList());
    }
}
