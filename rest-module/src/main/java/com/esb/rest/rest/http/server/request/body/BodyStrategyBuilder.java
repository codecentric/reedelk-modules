package com.esb.rest.rest.http.server.request.body;

import com.esb.api.message.MimeType;
import com.esb.rest.rest.commons.HttpMethodContentTypeKey;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpMethod.POST;

public class BodyStrategyBuilder {

    private static final BodyStrategy<byte[]> DEFAULT = new Default();

    private static final Map<HttpMethodContentTypeKey, BodyStrategy<?>> STRATEGIES;

    static {
        STRATEGIES = new HashMap<>();
        STRATEGIES.put(new HttpMethodContentTypeKey(POST.name(), MULTIPART_FORM_DATA.toString()), new PostMultipartFormData());
        STRATEGIES.put(new HttpMethodContentTypeKey(POST.name(), MULTIPART_MIXED.toString()), new PostMultipartMixed());
        STRATEGIES.put(new HttpMethodContentTypeKey(POST.name(), APPLICATION_X_WWW_FORM_URLENCODED.toString()), new PostApplicationWwwFormUrlencoded());
    }

    @SuppressWarnings("unchecked")
    public static <T> BodyStrategy<T> from(Class<T> clazz, FullHttpRequest request) {
        for (Map.Entry<HttpMethodContentTypeKey,BodyStrategy<?>> strategy : STRATEGIES.entrySet()) {

            HttpMethodContentTypeKey key = strategy.getKey();
            String contentType = key.getValue2();
            String method = key.getValue1();

            MimeType keyMimeType = MimeType.parse(contentType);
            MimeType requestMimeType = getMimeType(request);

            if (method.equalsIgnoreCase(request.method().name()) && sameMimeType(keyMimeType, requestMimeType)) {
                return (BodyStrategy<T>) strategy.getValue();
            }
        }
        return (BodyStrategy<T>) DEFAULT;
    }

    private static MimeType getMimeType(HttpRequest request) {
        return MimeType.parse(request.headers().get(HttpHeaderNames.CONTENT_TYPE));
    }


     /** Compares two given Mime Types only on primary and subtype. */
    private static boolean sameMimeType(MimeType mimeType1, MimeType mimeType2) {
        return mimeType1.getPrimaryType().equalsIgnoreCase(mimeType2.getPrimaryType()) &&
                mimeType1.getSubType().equalsIgnoreCase(mimeType2.getSubType());

    }

}
