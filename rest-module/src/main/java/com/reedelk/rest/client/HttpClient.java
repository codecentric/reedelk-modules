package com.reedelk.rest.client;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;

import java.io.IOException;

import static com.reedelk.rest.client.strategy.NoOpCallback.INSTANCE;
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

    public void close() throws IOException {
        delegate.close();
    }

    public void start() {
        this.delegate.start();
    }

    public void execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<Void> responseConsumer) {
        if (context != null) {
            delegate.execute(requestProducer, responseConsumer, context, INSTANCE);
        } else {
            delegate.execute(requestProducer, responseConsumer, INSTANCE);
        }
    }
}
