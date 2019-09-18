package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import com.reedelk.rest.client.HeaderProvider;
import com.reedelk.rest.client.UriProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.nio.client.HttpAsyncClient;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * Strategy for methods without a body.
 */
abstract class BaseStrategy implements Strategy {

    @Override
    public void execute(HttpAsyncClient client,
                        UriProvider uriProvider,
                        BodyProvider bodyProvider,
                        HeaderProvider headerProvider,
                        OnResult callback,
                        FlowContext flowContext) {
        HttpRequestBase baseRequest = request();
        baseRequest.setURI(uriProvider.uri());
        headerProvider.headers().forEach(baseRequest::addHeader);

        client.execute(
                new EmptyStreamRequestProducer(extractHost(uriProvider.uri()), baseRequest),
                new StreamResponseConsumer(callback, flowContext),
                NoOpCallback.INSTANCE);
    }

    protected abstract HttpRequestBase request();

}
