package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import com.reedelk.rest.client.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.reactivestreams.Publisher;

import java.net.URI;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * Strategy for methods with a body.
 */
abstract class BaseStrategyWithBody implements Strategy {

    @Override
    public void execute(HttpAsyncClient client,
                        OnResult callback, FlowContext flowContext, URIProvider URIProvider,
                        HeaderProvider headerProvider, BodyProvider bodyProvider) {

        HttpEntityEnclosingRequestBase request = request(bodyProvider);

        URI uri = URIProvider.uri();

        request.setURI(uri);
        request.setEntity(new BasicHttpEntity());
        headerProvider.headers().forEach(request::addHeader);

        Publisher<byte[]> body = bodyProvider.body();

        client.execute(
                new StreamRequestProducer(extractHost(uri), request, body),
                new StreamResponseConsumer(callback, flowContext),
                NoOpCallback.INSTANCE);
    }

    protected abstract HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider);
}
