package com.reedelk.rest.server;

import com.reedelk.rest.commons.OutboundProperty;
import com.reedelk.rest.configuration.RestListenerErrorResponse;
import com.reedelk.rest.configuration.RestListenerResponse;
import com.reedelk.runtime.api.message.Context;
import com.reedelk.runtime.api.message.Message;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.netty.http.server.HttpServerResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.valueOf;

public class MessageToHttpResponse {


    public static void from(Throwable exception,
                            Context context,
                            HttpServerResponse response,
                            RestListenerErrorResponse listenerErrorResponse) {

        // Handle status

        // Handle content type

        // Handle additional headers

    }


    public static void from(Message message,
                            Context context,
                            HttpServerResponse response,
                            RestListenerResponse listenerResponse) {

        // Handle status

        // Handle content type

        // Handle additional headers


        Map<String, String> responseHeaders = listenerResponse.getHeaders();
        Integer status = listenerResponse.getStatus();

        // Determining the Content type of the response:
        // 1. If exists a header specifying content type, than use that one,
        // 2. otherwise use the content type from the Message payload
        // 3. otherwise unknown

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
