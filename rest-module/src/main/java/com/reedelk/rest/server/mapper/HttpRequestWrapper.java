package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.AsSerializableMap;
import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.rest.commons.QueryParameters;
import com.reedelk.runtime.api.message.type.MimeType;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

class HttpRequestWrapper {

    private final HttpServerRequest request;

    HttpRequestWrapper(HttpServerRequest request) {
        this.request = request;
    }

    String version() {
        return request.version().text();
    }

    String scheme() {
        return request.scheme();
    }

    String method() {
        return request.method().name();
    }

    String requestUri() {
        return request.uri();
    }

    MimeType mimeType() {
        return MimeTypeExtract.from(request);
    }

    String queryString() {
        // Keep only query parameters from the uri
        int queryParamsStart = request.uri().indexOf("?");
        return queryParamsStart > -1 ?
                request.uri().substring(queryParamsStart + 1) :
                "";
    }

    String requestPath() {
        // Remove query parameters from the uri
        int queryParamsStart = request.uri().indexOf("?");
        return queryParamsStart > -1 ?
                request.uri().substring(0, queryParamsStart) :
                request.uri();
    }

    ByteBufFlux data() {
        return request.receive();
    }

    String remoteAddress() {
        return request.remoteAddress().toString();
    }

    HashMap<String, List<String>> queryParams() {
        return QueryParameters.from(request.uri());
    }

    HashMap<String, String> params() {
        return AsSerializableMap.of(request.params());
    }

    TreeMap<String, String> headers() {
        return HttpHeadersAsMap.of(request.requestHeaders());
    }
}
