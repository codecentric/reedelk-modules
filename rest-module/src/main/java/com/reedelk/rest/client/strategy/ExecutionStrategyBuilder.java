package com.reedelk.rest.client.strategy;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.StreamingMode;
import org.apache.http.client.methods.*;

import static com.reedelk.rest.commons.RestMethod.*;
import static com.reedelk.rest.configuration.StreamingMode.*;
import static java.lang.String.format;

public class ExecutionStrategyBuilder {

    private StreamingMode streaming;
    private RestMethod method;

    private ExecutionStrategyBuilder() {
    }

    public static ExecutionStrategyBuilder builder() {
        return new ExecutionStrategyBuilder();
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
            return new StrategyWithoutBody(HttpGet::new);
        } else if (HEAD.equals(method)) {
            return new StrategyWithoutBody(HttpHead::new);
        } else if (OPTIONS.equals(method)) {
            return new StrategyWithoutBody(HttpOptions::new);
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
        if (NONE.equals(streaming)) {
            return new StrategyWithBody(requestFactory);
        } else if (ALWAYS.equals(streaming)) {
            return new StrategyWithStreamBody(requestFactory);
        } else if (AUTO.equals(streaming)) {
            return new StrategyWithAutoStreamBody(requestFactory);
        } else {
            throw new IllegalArgumentException(format("Execution strategy not available for streaming mode '%s'", streaming));
        }
    }
}
