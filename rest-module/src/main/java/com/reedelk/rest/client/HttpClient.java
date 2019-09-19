package com.reedelk.rest.client;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class HttpClient {

    private final CloseableHttpAsyncClient delegate;
    private final HttpClientContext context;

    HttpClient(CloseableHttpAsyncClient delegate) {
        this.delegate = requireNonNull(delegate, "delegate http client");
        this.context = null;
    }

    HttpClient(CloseableHttpAsyncClient delegate, HttpClientContext context) {
        this.delegate = delegate;
        this.context = context;
    }

    public void execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<Void> responseConsumer) {
        if (context != null) {
            delegate.execute(requestProducer, responseConsumer, context, NO_OP_CALLBACK);
        } else {
            delegate.execute(requestProducer, responseConsumer, NO_OP_CALLBACK);
        }
    }

    void close() throws IOException {
        delegate.close();
    }

    void start() {
        this.delegate.start();
    }

    private static final FutureCallback<Void> NO_OP_CALLBACK = new FutureCallback<Void>() {
        @Override
        public void completed(Void result) {
        }

        @Override
        public void failed(Exception ex) {
        }

        @Override
        public void cancelled() {
        }
    };
}
