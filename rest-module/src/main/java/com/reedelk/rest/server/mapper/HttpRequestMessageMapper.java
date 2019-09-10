package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.TypedContent;
import reactor.netty.http.server.HttpServerRequest;

import static com.reedelk.rest.server.mapper.HttpRequestAttribute.*;

public class HttpRequestMessageMapper {

    private final String matchingPath;

    public HttpRequestMessageMapper(String matchingPath) {
        this.matchingPath = matchingPath;
    }

    public Message map(HttpServerRequest httpRequest) {
        HttpRequestWrapper request = new HttpRequestWrapper(httpRequest);

        HttpRequestAttributes requestAttributes = new HttpRequestAttributes();
        requestAttributes.put(matchingPath(), matchingPath);
        requestAttributes.put(method(), request.method());
        requestAttributes.put(scheme(), request.scheme());
        requestAttributes.put(headers(), request.headers());
        requestAttributes.put(version(), request.version());
        requestAttributes.put(pathParams(), request.params());
        requestAttributes.put(requestUri(), request.requestUri());
        requestAttributes.put(requestPath(), request.requestPath());
        requestAttributes.put(queryParams(), request.queryParams());
        requestAttributes.put(queryString(), request.queryString());
        requestAttributes.put(remoteAddress(), request.remoteAddress());

        TypedContent content = HttpRequestContentMapper.map(request);

        return MessageBuilder.get()
                .attributes(requestAttributes)
                .typedContent(content)
                .build();
    }
}
