package com.esb.foonnel.rest.http.request.method;

import com.esb.foonnel.api.message.*;
import com.esb.foonnel.rest.commons.InboundProperty;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esb.foonnel.api.message.MimeType.ANY;
import static com.esb.foonnel.api.message.MimeType.parse;
import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class PostRequest extends AbstractStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PostRequest.class);

    @Override
    protected Message handle0(Message inMessage, FullHttpRequest request) throws IOException {
        String contentType = InboundProperty.Headers.CONTENT_TYPE.get(inMessage);

        // 1. multipart/form-data (HttpPostRequestDecoder)
        if (is(contentType, HttpHeaderValues.MULTIPART_FORM_DATA)) {
            return handleWithPostDecoder(inMessage, request);
        }

        // 2. x-www-form-urlencoded (HttpPostRequestDecoder)
        if (is(contentType, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)) {
            return handleWithPostDecoder(inMessage, request);
        }

        // 3. multipart/mixed (HttpPostRequestDecoder)
        if (is(contentType, HttpHeaderValues.MULTIPART_MIXED)) {
            return handleWithPostDecoder(inMessage, request);
        }

        // 4. Handle the body content (application/octet-stream, image/jpg)
        TypedContent<byte[]> content = extractBodyContent(inMessage, request);
        inMessage.setContent(content);

        return inMessage;
    }

    private Message handleWithPostDecoder(Message inMessage, FullHttpRequest request) throws IOException {
        // POST Request Decoder to be used
        HttpPostRequestDecoder decoder = null;
        try {
            decoder = new HttpPostRequestDecoder(request);
            List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();

            Map<String,String> postedData = new HashMap<>();
            for (InterfaceHttpData httpData : bodyHttpDatas) {
                if (httpData.getHttpDataType() == HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) httpData;
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    postedData.put(name, value);
                }
                if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
                    Part messagePart = handleFileUpload((FileUpload) httpData);
                    inMessage.addPart(messagePart);
                }
                if (httpData.getHttpDataType() == HttpDataType.InternalAttribute) {
                    logger.warn("InternalAttribute");

                }

                decoder.removeHttpDataFromClean(httpData);
            }
            Type type = new Type(ANY, postedData.getClass());
            TypedContent<Map<String,String>> content = new MemoryTypedContent<>(postedData, type);
            inMessage.setContent(content);
            return inMessage;

        } catch (IOException exception) {
            logger.error("POST request handler", exception);
            throw exception;
        } finally {
            if (decoder != null) decoder.destroy();
        }
    }

    private Part handleFileUpload(FileUpload fileUpload) throws IOException {
        byte[] bytes = fileUpload.get();
        Type contentType = new Type(parse(fileUpload.getContentType()), byte[].class);
        MemoryTypedContent<byte[]> content = new MemoryTypedContent<>(bytes, contentType);
        return new Part(content);
    }

    private boolean is(String contentType, AsciiString targetContentType) {
        return contentType.contains(targetContentType);
    }



}
