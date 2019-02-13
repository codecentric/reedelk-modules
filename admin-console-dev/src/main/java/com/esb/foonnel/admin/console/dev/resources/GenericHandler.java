package com.esb.foonnel.admin.console.dev.resources;

import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import static com.esb.foonnel.admin.console.dev.PathParam.APP_PATH;
import static java.net.HttpURLConnection.HTTP_OK;

public class GenericHandler implements Take {

    private final Operation operation;

    private GenericHandler(Operation operation) {
        this.operation = operation;
    }

    static GenericHandler handlerFor(Operation operation) {
        return new GenericHandler(operation);
    }

    @Override
    public Response act(Request request) throws IOException {
        String encodedAppPath = APP_PATH.get(request);
        operation.run(decode(encodedAppPath));
        return new RsWithStatus(HTTP_OK);
    }

    public interface Operation {
        void run(String filePath);
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalStateException(var2);
        }
    }

}
