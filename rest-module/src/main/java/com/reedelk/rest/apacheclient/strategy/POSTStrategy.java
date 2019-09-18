package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import com.reedelk.rest.apacheclient.HeaderProvider;
import com.reedelk.rest.apacheclient.UriProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.client.HttpAsyncClient;

import static com.reedelk.rest.apacheclient.strategy.AbstractStrategy.NO_OP_CALLBACK;
import static org.apache.http.client.utils.URIUtils.extractHost;

public class POSTStrategy implements Strategy {

    @Override
    public void execute(HttpAsyncClient client, UriProvider uriProvider, BodyProvider bodyProvider, HeaderProvider headerProvider, OnResult callback, FlowContext flowContext) {
        HttpPost post = new HttpPost();
        post.setURI(uriProvider.uri());
        post.setEntity(new BasicHttpEntity());
        headerProvider.headers().forEach(post::addHeader);

        client.execute(
                new StreamRequestProducer(extractHost(uriProvider.uri()), post, bodyProvider.body()),
                new StreamResponseConsumer(callback, flowContext),
                NO_OP_CALLBACK);
    }
}
