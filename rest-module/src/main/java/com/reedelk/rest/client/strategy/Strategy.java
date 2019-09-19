package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import com.reedelk.rest.client.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.nio.client.HttpAsyncClient;

public interface Strategy {

    void execute(
            HttpAsyncClient client, OnResult callback, FlowContext flowContext,
            URIProvider URIProvider, HeaderProvider headerProvider, BodyProvider bodyProvider);
}
