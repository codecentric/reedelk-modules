package com.reedelk.rest.client;

import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;

import java.io.IOException;
import java.net.URI;

import static com.reedelk.rest.commons.Messages.RestClient;
import static com.reedelk.rest.commons.Messages.formatMessage;
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

    public void execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<Void> responseConsumer, ResultCallback resultCallback) {
        if (context != null) {
            delegate.execute(requestProducer, responseConsumer, context, resultCallback);
        } else {
            delegate.execute(requestProducer, responseConsumer, resultCallback);
        }
    }

    void close() throws IOException {
        delegate.close();
    }

    void start() {
        this.delegate.start();
    }

    public static class ResultCallback implements FutureCallback<Void> {

        private final URI requestUri;
        private final OnResult delegate;
        private final FlowContext flowContext;

        public ResultCallback(OnResult delegate, FlowContext flowContext, URI requestUri) {
            this.delegate = delegate;
            this.requestUri =  requestUri;
            this.flowContext = flowContext;
        }

        @Override
        public void completed(Void result) {
            // nothing to do. Already handled by the ResponseConsumer.
        }

        @Override
        public void failed(Exception exception) {
            delegate.onError(
                    new ESBException(formatMessage(RestClient.REQUEST_FAILED, requestUri), exception),
                    flowContext);
        }

        @Override
        public void cancelled() {
            delegate.onError(
                    new ESBException(formatMessage(RestClient.REQUEST_CANCELLED, requestUri)),
                    flowContext);
        }
    }
}
