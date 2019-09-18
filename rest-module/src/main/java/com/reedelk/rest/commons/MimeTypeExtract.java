package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.type.MimeType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRequest;

public class MimeTypeExtract {

    private static final Logger logger = LoggerFactory.getLogger(MimeTypeExtract.class);

    public static MimeType from(HttpServerRequest request) {
        return from(request.requestHeaders());
    }

    // TODO: Test it, it MUST be case insensitive!!!
    public static MimeType from(HttpHeaders headers) {
        if (headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
            try {
                return MimeType.parse(contentType);
            } catch (Exception e) {
                logger.warn(String.format("Could not parse content type '%s'", contentType), e);
            }
        }
        return MimeType.UNKNOWN;
    }

    public static MimeType from(Header[] headers) {
        for (Header header : headers) {
            if(HttpHeader.CONTENT_TYPE.equals(header.getName())) {
                String contentType = header.getValue();
                try {
                    return MimeType.parse(contentType);
                } catch (Exception e) {
                    logger.warn(String.format("Could not parse content type '%s'", contentType), e);
                    return MimeType.UNKNOWN;
                }
            }
        }
        return MimeType.UNKNOWN;
    }
}
