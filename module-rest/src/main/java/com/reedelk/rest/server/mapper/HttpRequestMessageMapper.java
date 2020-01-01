package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.rest.component.RestListener;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import reactor.netty.http.server.HttpServerRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.reedelk.rest.server.mapper.HttpRequestAttribute.*;
import static com.reedelk.runtime.api.message.MessageAttributeKey.CORRELATION_ID;
import static com.reedelk.runtime.api.message.content.MimeType.MULTIPART_FORM_DATA;

public class HttpRequestMessageMapper {

    private final String matchingPath;

    public HttpRequestMessageMapper(String matchingPath) {
        this.matchingPath = matchingPath;
    }

    public Message map(HttpServerRequest httpRequest) {
        HttpRequestWrapper request = new HttpRequestWrapper(httpRequest);

        Map<String, Serializable> attributes = new HashMap<>();
        attributes.put(MATCHING_PATH, matchingPath);
        attributes.put(METHOD, request.method());
        attributes.put(SCHEME, request.scheme());
        attributes.put(HEADERS, request.headers());
        attributes.put(VERSION, request.version());
        attributes.put(PATH_PARAMS, request.params());
        attributes.put(REQUEST_URI, request.requestUri());
        attributes.put(REQUEST_PATH, request.requestPath());
        attributes.put(QUERY_PARAMS, request.queryParams());
        attributes.put(QUERY_STRING, request.queryString());
        attributes.put(REMOTE_ADDRESS, request.remoteAddress());

        // We must set the correlation ID in the Attributes if X-Correlation-ID header is
        // present in the Request Headers, so that the Flow context can use it to set
        // the 'correlationId' context variable available in each flow execution instance.
        setCorrelationIdIfPresent(request, attributes);

        DefaultMessageAttributes requestAttributes = new DefaultMessageAttributes(RestListener.class, attributes);

        MimeType mimeType = request.mimeType();

        TypedContent<?> content = MULTIPART_FORM_DATA.equals(mimeType) ?
                HttpRequestMultipartFormDataMapper.map(request) :
                HttpRequestContentMapper.map(request);

        return MessageBuilder.get()
                .attributes(requestAttributes)
                .typedContent(content)
                .build();
    }

    private void setCorrelationIdIfPresent(HttpRequestWrapper request, Map<String, Serializable> attributes) {
        if (request.headers().containsKey(HttpHeader.X_CORRELATION_ID)) {
            attributes.put(CORRELATION_ID, request.headers().get(HttpHeader.X_CORRELATION_ID).get(0));
        }
    }
}
