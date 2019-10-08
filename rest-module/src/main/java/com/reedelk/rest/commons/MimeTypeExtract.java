package com.reedelk.rest.commons;

import com.reedelk.runtime.api.message.type.MimeType;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRequest;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;

public class MimeTypeExtract {

    private static final Logger logger = LoggerFactory.getLogger(MimeTypeExtract.class);

    private MimeTypeExtract() {
    }

    public static MimeType from(HttpServerRequest request) {
        return from(request.requestHeaders());
    }

    public static MimeType from(HttpHeaders headers) {
        if (headers != null && headers.contains(CONTENT_TYPE)) {
            String contentType = headers.get(CONTENT_TYPE);
            try {
                return MimeType.parse(contentType);
            } catch (Exception e) {
                logger.warn(String.format("Could not parse content type '%s'", contentType), e);
            }
        }
        return MimeType.UNKNOWN;
    }

    public static MimeType from(Header[] headers) {
        if (headers != null) {
            for (Header header : headers) {
                if(CONTENT_TYPE.equalsIgnoreCase(header.getName())) {
                    String contentType = header.getValue();
                    try {
                        return MimeType.parse(contentType);
                    } catch (Exception e) {
                        logger.warn(String.format("Could not parse content type '%s'", contentType), e);
                        return MimeType.UNKNOWN;
                    }
                }
            }
        }
        return MimeType.UNKNOWN;
    }
}
