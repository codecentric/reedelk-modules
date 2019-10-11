package com.reedelk.rest.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import reactor.netty.http.HttpDecoderSpec;

public class Defaults {

    private Defaults() {
    }

    public class RestClient {

        private RestClient() {
        }

        public static final int RESPONSE_BUFFER_SIZE = 16 * 1024;
        public static final int REQUEST_BUFFER_SIZE = 16 * 1024;
    }

    public static class RestListener {

        private RestListener() {
        }

        public static int port(Integer actual) {
            return actual == null ? DEFAULT_PORT : actual;
        }

        public static String host(String actual) {
            return StringUtils.isBlank(actual) ? DEFAULT_HOST : actual;
        }

        public static boolean compress(Boolean actual) {
            return actual == null ? DEFAULT_COMPRESS : actual;
        }

        public static int maxChunkSize(Integer actual) {
            return actual == null ? DEFAULT_MAX_CHUNK_SIZE : actual;
        }

        public static int maxHeaderSize(Integer actual) {
            return actual == null ? DEFAULT_MAX_HEADER_SIZE : actual;
        }

        private static final boolean DEFAULT_COMPRESS =  false;
        private static final String DEFAULT_HOST = "localhost";
        private static final int DEFAULT_PORT = 8080;
        private static final int DEFAULT_MAX_CHUNK_SIZE = HttpDecoderSpec.DEFAULT_MAX_CHUNK_SIZE;
        private static final int DEFAULT_MAX_HEADER_SIZE = HttpDecoderSpec.DEFAULT_MAX_HEADER_SIZE;
    }
}
