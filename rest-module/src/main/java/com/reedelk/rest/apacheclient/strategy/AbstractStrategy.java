package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import com.reedelk.rest.apacheclient.HeaderProvider;
import com.reedelk.rest.apacheclient.UriProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.nio.client.HttpAsyncClient;

import static org.apache.http.client.utils.URIUtils.extractHost;

abstract class AbstractStrategy implements Strategy {

    static final NoOpCallback NO_OP_CALLBACK = new NoOpCallback();

    @Override
    public void execute(HttpAsyncClient client,
                        UriProvider uriProvider,
                        BodyProvider bodyProvider,
                        HeaderProvider headerProvider,
                        OnResult callback,
                        FlowContext flowContext) {
        HttpRequestBase baseRequest = baseRequest(bodyProvider);
        baseRequest.setURI(uriProvider.uri());
        headerProvider.headers().forEach(baseRequest::addHeader);

        client.execute(
                new EmptyStreamRequestProducer(extractHost(uriProvider.uri()), baseRequest),
                new StreamResponseConsumer(callback, flowContext),
                NO_OP_CALLBACK);
    }

    protected abstract HttpRequestBase baseRequest(BodyProvider bodyProvider);

}
