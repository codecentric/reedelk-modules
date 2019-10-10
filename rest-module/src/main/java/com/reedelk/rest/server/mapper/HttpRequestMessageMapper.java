package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.TypedContent;
import reactor.netty.http.server.HttpServerRequest;

import static com.reedelk.rest.server.mapper.HttpRequestAttribute.*;
import static com.reedelk.runtime.api.message.type.MimeType.MULTIPART_FORM_DATA;

public class HttpRequestMessageMapper {

    private final String matchingPath;

    public HttpRequestMessageMapper(String matchingPath) {
        this.matchingPath = matchingPath;
    }

    public Message map(HttpServerRequest httpRequest) {
        HttpRequestWrapper request = new HttpRequestWrapper(httpRequest);

        HttpRequestAttributes requestAttributes = new HttpRequestAttributes();
        requestAttributes.put(MATCHING_PATH, matchingPath);
        requestAttributes.put(METHOD, request.method());
        requestAttributes.put(SCHEME, request.scheme());
        requestAttributes.put(HEADERS, request.headers());
        requestAttributes.put(VERSION, request.version());
        requestAttributes.put(PATH_PARAMS, request.params());
        requestAttributes.put(REQUEST_URI, request.requestUri());
        requestAttributes.put(REQUEST_PATH, request.requestPath());
        requestAttributes.put(QUERY_PARAMS, request.queryParams());
        requestAttributes.put(QUERY_STRING, request.queryString());
        requestAttributes.put(REMOTE_ADDRESS, request.remoteAddress());

        MimeType mimeType = request.mimeType();

        TypedContent content = MULTIPART_FORM_DATA.equals(mimeType) ?
                HttpRequestMultipartMapper.map(request) :
                HttpRequestContentMapper.map(request);

        return MessageBuilder.get()
                .attributes(requestAttributes)
                .typedContent(content)
                .build();
    }
}
