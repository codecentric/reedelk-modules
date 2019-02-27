package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.message.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esb.foonnel.api.message.MimeType.*;
import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class PostRequest extends AbstractStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PostRequest.class);

    @Override
    protected Message handle0(Message inMessage, FullHttpRequest request) throws IOException {
        // Following cases:
        // 1. multipart/form-data (HttpPostRequestDecoder)
        // 2. x-www-form-urlencoded (HttpPostRequestDecoder)
        // 3. Handle the body as if it was HttpGET

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
                    decoder.removeHttpDataFromClean(httpData);
                }
                if (httpData.getHttpDataType() == HttpDataType.InternalAttribute) {
                    logger.warn("InternalAttribute");

                }
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

}
