package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;

import java.net.URI;

/**
 * Strategy for methods without a body: GET, OPTIONS, HEAD
 */
public class StrategyWithoutBody implements Strategy {

    private final int responseBufferSize;
    private final RequestWithoutBodyFactory requestFactory;

    StrategyWithoutBody(RequestWithoutBodyFactory requestFactory, int responseBufferSize) {
        this.requestFactory = requestFactory;
        this.responseBufferSize = responseBufferSize;
    }

    @Override
    public void execute(HttpClient client, OnResult callback, Message input, FlowContext flowContext, URIProvider uriProvider, HeaderProvider headerProvider, BodyProvider bodyProvider) {
        URI uri = uriProvider.uri();

        HttpRequestBase baseRequest = requestFactory.create();

        baseRequest.setURI(uri);

        headerProvider.headers().forEach(baseRequest::addHeader);

        HttpHost httpHost = URIUtils.extractHost(uri);

        client.execute(
                new EmptyStreamRequestProducer(httpHost, baseRequest),
                new StreamResponseConsumer(callback, flowContext, responseBufferSize),
                new HttpClient.ResultCallback(callback, flowContext, uri));
    }
}
