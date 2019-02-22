package com.esb.foonnel.rest.mapper;

import com.esb.foonnel.api.Message;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpRequestToMessage implements Mapper<FullHttpRequest, Message> {

    @Override
    public Message map(FullHttpRequest request) {
        String uri = request.uri();
        String method = request.method().name();
        String content = request.content().toString(UTF_8);
        Map<String,String> headers = asMap(request.headers());

        Message message = new Message();
        message.setContent(content);
        message.setRequestPath(uri);
        message.setRequestMethod(method);
        message.setRequestHttpHeaders(headers);
        return message;
    }

    private Map<String,String> asMap(HttpHeaders headers) {
        Map<String,String> requestHeaders = new HashMap<>();
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headers.get(headerName)));
        return requestHeaders;
    }

}
