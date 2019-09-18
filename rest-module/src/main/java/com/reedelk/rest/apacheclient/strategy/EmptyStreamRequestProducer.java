package com.reedelk.rest.apacheclient.strategy;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;

class EmptyStreamRequestProducer extends BasicAsyncRequestProducer {

    public EmptyStreamRequestProducer(HttpHost target, HttpRequest request) {
        super(target, request);
    }
}
