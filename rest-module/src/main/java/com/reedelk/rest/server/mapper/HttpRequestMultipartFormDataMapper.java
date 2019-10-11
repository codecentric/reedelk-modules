package com.reedelk.rest.server.mapper;

import com.reedelk.rest.ExecutionException;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.content.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.reedelk.rest.commons.Messages.RestListener.*;
import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

class HttpRequestMultipartFormDataMapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestMultipartFormDataMapper.class);

    private HttpRequestMultipartFormDataMapper() {
    }

    static TypedContent map(HttpRequestWrapper request) {
        if (HttpMethod.POST != HttpMethod.valueOf(request.method()) ||
                HttpVersion.HTTP_1_1 != HttpVersion.valueOf(request.version())) {
            throw new ExecutionException(ERROR_MULTIPART_NOT_SUPPORTED.format());
        }

        Mono<Parts> partsMono = request.data().aggregate().flatMap(byteBuffer -> {

            // POST Multipart is only supported on HTTP 1.1 and for POST method.
            FullHttpRequest fullHttpRequest =
                    new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1,
                            HttpMethod.POST,
                            request.requestUri(),
                            byteBuffer,
                            request.requestHeaders(),
                            EmptyHttpHeaders.INSTANCE);

            HttpPostRequestDecoder postDecoder =
                    new HttpPostRequestDecoder(
                            new DefaultHttpDataFactory(false),
                            fullHttpRequest,
                            CharsetUtil.UTF_8);

            Parts parts = new Parts();

            // Loop attribute/file upload parts
            for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                if (HttpDataType.Attribute == data.getHttpDataType()) {
                    handleAttributePart(parts, (Attribute) data);

                } else if (HttpDataType.FileUpload == data.getHttpDataType()) {
                    handleFileUploadPart(parts, (FileUpload) data);
                }
            }
            postDecoder.destroy();
            fullHttpRequest.release();
            return Mono.just(parts);
        });

        return new MultipartContent(partsMono, MimeType.MULTIPART_FORM_DATA);
    }

    private static void handleFileUploadPart(Parts parts, FileUpload fileUpload) {
        String name = fileUpload.getName();
        String filename = fileUpload.getFilename();
        String contentType = fileUpload.getContentType();
        String contentTransferEncoding = fileUpload.getContentTransferEncoding();

        MimeType mimeType = MimeType.parse(contentType);

        byte[] fileContentAsBytes;
        try {
            fileContentAsBytes = fileUpload.get();
        } catch (IOException e) {
            ESBException rethrown = new ESBException(ERROR_MULTIPART_FILE_UPLOAD_VALUE.format(name), e);
            logger.error("Multipart Mapper error", rethrown);
            throw rethrown;
        }

        ByteArrayContent content = new ByteArrayContent(fileContentAsBytes, mimeType);
        Part part = Part.builder()
                .content(content)
                .attribute(MultipartAttribute.TRANSFER_ENCODING, contentTransferEncoding)
                .attribute(MultipartAttribute.CONTENT_TYPE, contentType)
                .attribute(MultipartAttribute.FILE_NAME, filename)
                .name(name)
                .build();
        parts.put(name, part);
    }

    private static void handleAttributePart(Parts parts, Attribute attribute) {
        String name = attribute.getName();
        String attributeValue;
        try {
            attributeValue = attribute.getValue();
        } catch (IOException e) {
            ESBException rethrown = new ESBException(ERROR_MULTIPART_ATTRIBUTE_VALUE.format(name), e);
            logger.error("Multipart Mapper error", rethrown);
            throw rethrown;
        }

        StringContent content = new StringContent(attributeValue, MimeType.TEXT);
        Part part = Part.builder()
                .content(content)
                .name(name)
                .build();
        parts.put(name, part);
    }
}
