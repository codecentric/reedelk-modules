package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.apache.http.client.methods.HttpRequestBase;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * Strategy for methods without a body.
 */
abstract class BaseStrategy implements Strategy {

    @Override
    public void execute(HttpClient client,
                        OnResult callback, Message input, FlowContext flowContext, URIProvider URIProvider,
                        HeaderProvider headerProvider, BodyProvider bodyProvider) {

        HttpRequestBase baseRequest = request();
        baseRequest.setURI(URIProvider.uri());
        headerProvider.headers().forEach(baseRequest::addHeader);

        client.execute(new EmptyStreamRequestProducer(extractHost(URIProvider.uri()), baseRequest),
                new StreamResponseConsumer(callback, flowContext));
    }

    protected abstract HttpRequestBase request();

}
