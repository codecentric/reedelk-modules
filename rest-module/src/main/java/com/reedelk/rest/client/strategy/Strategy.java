package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import com.reedelk.rest.client.HeaderProvider;
import com.reedelk.rest.client.UriProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import org.apache.http.nio.client.HttpAsyncClient;

public interface Strategy {

    void execute(HttpAsyncClient client,
                 UriProvider uriProvider,
                 BodyProvider bodyProvider,
                 HeaderProvider headerProvider,
                 OnResult callback, FlowContext flowContext);
}
