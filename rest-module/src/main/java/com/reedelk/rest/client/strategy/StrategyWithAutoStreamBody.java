package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;

public class StrategyWithAutoStreamBody implements Strategy {

    private StrategyWithBody strategyWithBody;
    private StrategyWithStreamBody strategyWithStreamBody;

    StrategyWithAutoStreamBody(RequestWithBodyFactory requestFactory) {
        this.strategyWithBody = new StrategyWithBody(requestFactory);
        this.strategyWithStreamBody = new StrategyWithStreamBody(requestFactory);
    }

    @Override
    public void execute(HttpClient client, OnResult callback, Message input, FlowContext flowContext,
                        URIProvider URIProvider, HeaderProvider headerProvider, BodyProvider bodyProvider) {
        if (bodyProvider.streamable(input)) {
            this.strategyWithStreamBody.execute(client, callback, input, flowContext, URIProvider, headerProvider, bodyProvider);
        } else {
            this.strategyWithBody.execute(client, callback, input, flowContext, URIProvider, headerProvider, bodyProvider);
        }
    }
}
