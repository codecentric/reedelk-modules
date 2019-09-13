package com.reedelk.rest.client;

import com.reedelk.rest.commons.HttpHeadersAsMap;
import com.reedelk.rest.commons.MimeTypeExtract;
import com.reedelk.runtime.api.message.type.MimeType;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;
import java.util.TreeMap;

public class HttpResponseWrapper {

    private byte[] data;
    private HttpHeaders headers;
    private HttpResponseStatus status;

    public TreeMap<String, List<String>> headers() {
        return HttpHeadersAsMap.of(headers);
    }

    public void headers(HttpHeaders headers) {
        this.headers = headers;
    }

    public MimeType mimeType() {
        return MimeTypeExtract.from(headers);
    }

    public HttpResponseStatus status() {
        return status;
    }

    public void status(HttpResponseStatus status) {
        this.status = status;
    }

    public byte[] data() {
        return data;
    }

    public void data(byte[] data) {
        this.data = data;
    }
}
