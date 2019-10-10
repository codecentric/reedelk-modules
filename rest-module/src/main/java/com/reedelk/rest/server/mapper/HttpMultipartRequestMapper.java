package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.HttpHeader;
import com.reedelk.runtime.api.message.type.*;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class HttpMultipartRequestMapper {

    private HttpMultipartRequestMapper() {
    }

    /**
     * Given an http request, it finds the most suitable TypedContent for the request.
     * For example, it checks the mime type of the request and it converts it a String
     * if it is a text based mime type, otherwise it keeps as bytes.
     */
    static TypedContent map(HttpRequestWrapper request) {
        Mono<Parts> partsMono = request.data().aggregate().flatMap((Function<ByteBuf, Mono<Parts>>) byteBuf -> {

            FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, request.requestUri(), byteBuf, request.requestHeaders(), EmptyHttpHeaders.INSTANCE);

            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest, CharsetUtil.UTF_8);

            Parts parts = new Parts();

            // Loop parts
            for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                // attribute
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    String name = attribute.getName();
                    try {
                        StringContent content = new StringContent(attribute.getValue(), MimeType.TEXT);
                        parts.put(name, new Part(name, content));
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO:This  exception should be thrown and the request fail!
                    }
                }
                // upload
                else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {

                    FileUpload fileUpload = (FileUpload) data;
                    String name = fileUpload.getName();
                    String contentType = fileUpload.getContentType();
                    String contentTransferEncoding = fileUpload.getContentTransferEncoding();
                    String filename = fileUpload.getFilename();

                    MimeType mimeType = MimeType.parse(contentType);

                    try {
                        ByteArrayContent content = new ByteArrayContent(fileUpload.get(), mimeType);

                        Map<String, String> attrs = new HashMap<>();
                        attrs.put(HttpHeader.CONTENT_TYPE, contentType);
                        attrs.put(HttpHeader.TRANSFER_ENCODING, contentTransferEncoding);
                        attrs.put("filename", filename);

                        parts.put(name, new Part(name, content, attrs));
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO:This  exception should be thrown and the request fail!
                    }
                }
            }
            postDecoder.destroy();
            fullHttpRequest.release();
            return Mono.just(parts);
        });

        return new MultipartContent(partsMono);
    }
}
