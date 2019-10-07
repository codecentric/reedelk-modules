package com.reedelk.rest.client;

import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.reedelk.rest.commons.Messages.RestClient.REQUEST_CANCELLED;
import static com.reedelk.rest.commons.Messages.RestClient.REQUEST_FAILED;
import static java.util.Objects.requireNonNull;

public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

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

    public void close() {
        try {
            delegate.close();
        } catch (Exception e) {
            logger.warn("Error while closing http client", e);
        }
    }

    public void start() {
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
                    new ESBException(REQUEST_FAILED.format(requestUri, exception.getMessage()), exception),
                    flowContext);
        }

        @Override
        public void cancelled() {
            delegate.onError(
                    new ESBException(REQUEST_CANCELLED.format(requestUri)),
                    flowContext);
        }
    }
}
