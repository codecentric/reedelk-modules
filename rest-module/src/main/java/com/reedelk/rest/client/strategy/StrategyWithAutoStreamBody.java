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

    StrategyWithAutoStreamBody(RequestWithBodyFactory requestFactory, int requestBufferSize, int responseBufferSize) {
        this.strategyWithBody = new StrategyWithBody(requestFactory, responseBufferSize);
        this.strategyWithStreamBody = new StrategyWithStreamBody(requestFactory, requestBufferSize, responseBufferSize);
    }

    @Override
    public void execute(HttpClient client, OnResult callback, Message input, FlowContext flowContext,
                        URIProvider URIProvider, HeaderProvider headerProvider, BodyProvider bodyProvider) {
        if (bodyProvider.streamable(input)) {
            strategyWithStreamBody.execute(client, callback, input, flowContext,
                    URIProvider, headerProvider, bodyProvider);
        } else {
            strategyWithBody.execute(client, callback, input, flowContext,
                    URIProvider, headerProvider, bodyProvider);
        }
    }
}
