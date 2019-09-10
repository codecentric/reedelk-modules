package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.AsSerializableMap;
import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.rest.commons.QueryParameters;
import com.reedelk.runtime.api.message.type.MimeType;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class HttpRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestWrapper.class);

    private final HttpServerRequest request;

    public HttpRequestWrapper(HttpServerRequest request) {
        this.request = request;
    }

    public String uri() {
        return request.uri();
    }

    public String method() {
        return request.method().name();
    }

    public ByteBufFlux receive() {
        return request.receive();
    }

    public MimeType mimeType() {
        return mimeTypeOf(request);
    }

    public TreeMap<String, String> headers() {
        return HttpHeadersAsMap.of(request.requestHeaders());
    }

    public HashMap<String,List<String>> queryParams() {
       return QueryParameters.from(request.uri());
    }

    public HashMap<String, String> params() {
        return AsSerializableMap.of(request.params());
    }

    private static MimeType mimeTypeOf(HttpServerRequest request) {
        if (request.requestHeaders().contains(HttpHeaderNames.CONTENT_TYPE)) {
            String contentType = request.requestHeaders().get(HttpHeaderNames.CONTENT_TYPE);
            try {
                return MimeType.parse(contentType);
            } catch (Exception e) {
                logger.warn(String.format("Could not parse content type '%s'", contentType), e);
            }
        }
        return MimeType.UNKNOWN;
    }
}
