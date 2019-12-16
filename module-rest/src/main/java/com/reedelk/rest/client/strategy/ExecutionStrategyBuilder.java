package com.reedelk.rest.client.strategy;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.StreamingMode;
import com.reedelk.rest.configuration.client.AdvancedConfiguration;
import org.apache.http.client.methods.*;

import java.util.Optional;

import static com.reedelk.rest.commons.Defaults.RestClient.REQUEST_BUFFER_SIZE;
import static com.reedelk.rest.commons.Defaults.RestClient.RESPONSE_BUFFER_SIZE;
import static com.reedelk.rest.commons.RestMethod.*;
import static com.reedelk.rest.configuration.StreamingMode.*;
import static java.lang.String.format;

public class ExecutionStrategyBuilder {

    private AdvancedConfiguration advancedConfiguration;
    private StreamingMode streaming;
    private RestMethod method;

    private ExecutionStrategyBuilder() {
    }

    public static ExecutionStrategyBuilder builder() {
        return new ExecutionStrategyBuilder();
    }

    public ExecutionStrategyBuilder advancedConfig(AdvancedConfiguration advancedConfiguration) {
        this.advancedConfiguration = advancedConfiguration;
        return this;
    }

    public ExecutionStrategyBuilder streaming(StreamingMode streaming) {
        this.streaming = streaming;
        return this;
    }

    public ExecutionStrategyBuilder method(RestMethod method) {
        this.method = method;
        return this;
    }

    public Strategy build() {
        if (GET.equals(method)) {
            return new StrategyWithoutBody(HttpGet::new, getResponseBufferSize());
        } else if (HEAD.equals(method)) {
            return new StrategyWithoutBody(HttpHead::new, getResponseBufferSize());
        } else if (OPTIONS.equals(method)) {
            return new StrategyWithoutBody(HttpOptions::new, getResponseBufferSize());
        } else if (POST.equals(method)) {
            return strategyWithBody(HttpPost::new);
        } else if (PUT.equals(method)) {
            return strategyWithBody(HttpPut::new);
        } else if (DELETE.equals(method)) {
            return strategyWithBody(HttpDeleteWithBody::new);
        } else {
            throw new IllegalArgumentException(format("Strategy not available for method '%s'", method));
        }
    }

    private Strategy strategyWithBody(RequestWithBodyFactory requestFactory) {
        int responseBufferSize = getResponseBufferSize();
        if (NONE.equals(streaming)) {
            return new StrategyWithBody(requestFactory, responseBufferSize);
        } else if (ALWAYS.equals(streaming)) {
            int requestBufferSize = getRequestBufferSize();
            return new StrategyWithStreamBody(requestFactory, requestBufferSize, responseBufferSize);
        } else if (AUTO.equals(streaming)) {
            int requestBufferSize = getRequestBufferSize();
            return new StrategyWithAutoStreamBody(requestFactory, requestBufferSize, responseBufferSize);
        } else {
            throw new IllegalArgumentException(format("Execution strategy not available for streaming mode '%s'", streaming));
        }
    }

    private int getResponseBufferSize() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(config -> Optional.ofNullable(config.getResponseBufferSize()))
                .orElse(RESPONSE_BUFFER_SIZE);
    }

    private int getRequestBufferSize() {
        return Optional.ofNullable(advancedConfiguration)
                .flatMap(advancedConfiguration -> Optional.ofNullable(advancedConfiguration.getRequestBufferSize()))
                .orElse(REQUEST_BUFFER_SIZE);
    }
}
