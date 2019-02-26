package com.esb.foonnel.rest.http.strategies;

import com.esb.foonnel.api.message.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class POSTRequestStrategy extends AbstractStrategy {

    @Override
    protected Message handle0(Message inMessage, FullHttpRequest request) throws IOException {

        HttpPostRequestDecoder decoder = null;
        try {
            decoder = new HttpPostRequestDecoder(request);

            List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();

            for (InterfaceHttpData httpData : bodyHttpDatas) {


                if (httpData.getHttpDataType() == HttpDataType.Attribute) {
                    System.out.println("Attribute");
                }
                if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
                    System.out.println("FileUpload");
                    handleFileUpload((FileUpload) httpData);
                    decoder.removeHttpDataFromClean(httpData);
                }
                if (httpData.getHttpDataType() == HttpDataType.InternalAttribute) {
                    System.out.println("InternalAttribute");
                }
            }

            return inMessage;

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (decoder != null) decoder.destroy();
        }
    }

    private void handleFileUpload(FileUpload fileUpload) throws IOException {


        byte[] bytes = fileUpload.get();
        String name = fileUpload.getName();
        File file = fileUpload.getFile();

        String contentTransferEncoding = fileUpload.getContentTransferEncoding();
        boolean inMemory = fileUpload.isInMemory();

        ContentType contentType = new ContentType(MimeType.parse(fileUpload.getContentType()), byte[].class);
        MemoryTypedContent<byte[]> content = new MemoryTypedContent<>(bytes, contentType);
        Part part = new Part(content);

    }

}
