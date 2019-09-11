package com.reedelk.rest.commons;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtils {

    private static final Logger logger = LoggerFactory.getLogger(StackTraceUtils.class);

    public static Publisher<byte[]> asByteStream(Throwable exception) {
        String exceptionAsString = asString(exception);
        return exceptionAsString == null ?
                Mono.empty() :
                Mono.just(exceptionAsString.getBytes());
    }

    public static String asString(Throwable exception) {
        try (StringWriter stringWriter = new StringWriter()) {
            try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
                exception.printStackTrace(printWriter);
                return stringWriter.toString();
            }
        } catch (IOException e) {
            logger.error("Could not serialize exception as byte stream", e);
            return null;
        }
    }
}
