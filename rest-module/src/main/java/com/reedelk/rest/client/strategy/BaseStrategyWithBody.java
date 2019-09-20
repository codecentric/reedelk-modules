package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.reactivestreams.Publisher;

import java.net.URI;

import static org.apache.http.client.utils.URIUtils.extractHost;

/**
 * Strategy for methods with a body.
 */
abstract class BaseStrategyWithBody implements Strategy {

    private final boolean chunked;

    BaseStrategyWithBody(boolean chunked) {
        this.chunked = chunked;
    }

    @Override
    public void execute(HttpClient client,
                        OnResult callback, Message input, FlowContext flowContext, URIProvider URIProvider,
                        HeaderProvider headerProvider, BodyProvider bodyProvider) {

        URI uri = URIProvider.uri();

        HttpEntityEnclosingRequestBase request = request(bodyProvider);
        request.setURI(uri);
        headerProvider.headers().forEach(request::addHeader);

        if (chunked) {
            // Chunked: payload stream is streamed in chunks.
            // The content length header is not sent.
            request.setEntity(new BasicHttpEntity());
            Publisher<byte[]> body = bodyProvider.asStream(input, flowContext);
            client.execute(new StreamRequestProducer(extractHost(uri), request, body),
                    new StreamResponseConsumer(callback, flowContext));

        } else {
            // The content length header is sent.
            byte[] bodyAsByteArray = bodyProvider.asByteArray(input, flowContext);
            NByteArrayEntity byteArrayEntity = new NByteArrayEntity(bodyAsByteArray);
            request.setEntity(byteArrayEntity);
            client.execute(HttpAsyncMethods.create(extractHost(uri), request),
                    new StreamResponseConsumer(callback, flowContext));
        }
    }

    protected abstract HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider);
}
