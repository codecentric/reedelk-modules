package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.entity.NByteArrayEntity;

import java.net.URI;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * No streaming strategy. Content-Length header is sent.
 */
public class StrategyWithBody implements Strategy {

    private final int responseBufferSize;
    private final RequestWithBodyFactory requestFactory;

    StrategyWithBody(RequestWithBodyFactory requestFactory, int responseBufferSize) {
        this.requestFactory = requestFactory;
        this.responseBufferSize = responseBufferSize;
    }

    @Override
    public void execute(HttpClient client, OnResult callback, Message input, FlowContext flowContext,
                        URIProvider URIProvider, HeaderProvider headerProvider, BodyProvider bodyProvider) {

        URI uri = URIProvider.uri();

        byte[] body = bodyProvider.asByteArray(input, flowContext);

        NByteArrayEntity entity = new NByteArrayEntity(body);

        HttpEntityEnclosingRequestBase request = requestFactory.create();

        request.setURI(uri);

        request.setEntity(entity);

        headerProvider.headers().forEach(request::addHeader);

        client.execute(
                HttpAsyncMethods.create(extractHost(uri), request),
                new StreamResponseConsumer(callback, flowContext, responseBufferSize),
                new HttpClient.ResultCallback(callback, flowContext, uri));
    }
}
