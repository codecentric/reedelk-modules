package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import com.reedelk.rest.apacheclient.HeaderProvider;
import com.reedelk.rest.apacheclient.UriProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.client.HttpAsyncClient;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * Strategy for methods with a body.
 */
abstract class BaseStrategyWithBody implements Strategy {

    @Override
    public void execute(HttpAsyncClient client,
                        UriProvider uriProvider,
                        BodyProvider bodyProvider,
                        HeaderProvider headerProvider,
                        OnResult callback,
                        FlowContext flowContext) {

        HttpEntityEnclosingRequestBase request = request(bodyProvider);
        request.setURI(uriProvider.uri());
        request.setEntity(new BasicHttpEntity());
        headerProvider.headers().forEach(request::addHeader);

        client.execute(
                new StreamRequestProducer(extractHost(uriProvider.uri()), request, bodyProvider.body()),
                new StreamResponseConsumer(callback, flowContext),
                NoOpCallback.INSTANCE);
    }

    protected abstract HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider);
}
