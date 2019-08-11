package com.reedelk.rest.server;

import com.reedelk.rest.commons.OutboundProperty;
import com.reedelk.runtime.api.message.Message;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import reactor.netty.http.server.HttpServerResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;

public class MapMessageToHttpResponse {

    private final HttpVersion httpVersion;

    MapMessageToHttpResponse(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public static void from(Message message, HttpServerResponse response) {
        // TODO: Handle the  payload
        String contentType = TEXT_PLAIN.toString();
        Map<String, String> outboundHeaders = new HashMap<>();

        if (OutboundProperty.HEADERS.isDefined(message)) {
            outboundHeaders = OutboundProperty.HEADERS.getMap(message);
            if (outboundHeaders.containsKey(CONTENT_TYPE.toString())) {
                contentType = outboundHeaders.get(CONTENT_TYPE.toString());
            }
        }

        if (OutboundProperty.STATUS.isDefined(message)) {
            HttpResponseStatus httpStatus = valueOf(OutboundProperty.STATUS.getInt(message));
            response.status(httpStatus);
        }

        HttpHeaders headers = response.responseHeaders();
        headers.add(CONTENT_TYPE, contentType);

        for (Map.Entry<String, String> header : outboundHeaders.entrySet()) {
            getMatchingHeader(headers, header.getKey()).ifPresent(headers::remove);
            headers.set(header.getKey(), header.getValue());
        }
    }

    private static Optional<String> getMatchingHeader(HttpHeaders headers, String targetHeaderName) {
        for (String headerName : headers.names()) {
            if (headerName.toLowerCase().equals(targetHeaderName.toLowerCase())) {
                return Optional.of(headerName);
            }
        }
        return Optional.empty();
    }
}
