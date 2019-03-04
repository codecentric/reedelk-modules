package com.esb.rest.server.request.body;

import com.esb.api.message.MemoryTypedContent;
import com.esb.api.message.Part;
import com.esb.api.message.Type;
import com.esb.api.message.TypedContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esb.api.message.MimeType.ANY;
import static com.esb.api.message.MimeType.parse;
import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

class PostMultipartFormData implements BodyStrategy<Map<String,String>> {

    private static final Logger logger = LoggerFactory.getLogger(PostMultipartFormData.class);

    @Override
    public BodyStrategyResult<Map<String,String>> execute(FullHttpRequest request) throws Exception {

        // POST Request Decoder to be used
        HttpPostRequestDecoder decoder = null;
        try {
            decoder = new HttpPostRequestDecoder(request);

            List<Part> parts = new ArrayList<>();
            Map<String,String> postedData = new HashMap<>();

            List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();
            for (InterfaceHttpData httpData : bodyHttpDatas) {

                if (httpData.getHttpDataType() == HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) httpData;
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    postedData.put(name, value);
                }

                if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
                    Part messagePart = handleFileUpload((FileUpload) httpData);
                    parts.add(messagePart);
                }

                decoder.removeHttpDataFromClean(httpData);
            }

            Type type = new Type(ANY, postedData.getClass());
            TypedContent<Map<String,String>> content = new MemoryTypedContent<>(postedData, type);

            return new BodyStrategyResult<>(content, parts);

        } finally {
            destroy(decoder);
        }
    }

    private Part handleFileUpload(FileUpload fileUpload) throws IOException {
        byte[] bytes = fileUpload.get();// TODO: Handle is in memory or not!
        Type contentType = new Type(parse(fileUpload.getContentType()), byte[].class);
        MemoryTypedContent<byte[]> content = new MemoryTypedContent<>(bytes, contentType);
        return new Part(content);
    }

    private void destroy(HttpPostRequestDecoder decoder) {
        if (decoder != null) {
            try {
                decoder.destroy();
            } catch (Exception e) {
                logger.error("destroy post decoder", e);
            }
        }
    }

}
