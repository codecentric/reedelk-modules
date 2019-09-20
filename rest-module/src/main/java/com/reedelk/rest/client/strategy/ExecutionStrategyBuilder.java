package com.reedelk.rest.client.strategy;

import com.reedelk.rest.commons.RestMethod;

import static java.util.Objects.requireNonNull;

public class ExecutionStrategyBuilder {

    private RestMethod method;
    private boolean chunked;

    private ExecutionStrategyBuilder() {
    }

    public static ExecutionStrategyBuilder builder() {
        return new ExecutionStrategyBuilder();
    }

    public ExecutionStrategyBuilder method(RestMethod method) {
        this.method = requireNonNull(method, "method");
        return this;
    }

    public ExecutionStrategyBuilder chunked(Boolean chunked) {
        this.chunked = Boolean.TRUE.equals(chunked);
        return this;
    }

    public Strategy build() {
        if (RestMethod.GET.equals(method)) {
            return new GET();

        } else if (RestMethod.HEAD.equals(method)) {
            return new HEAD();

        } else if (RestMethod.OPTIONS.equals(method)) {
            return new OPTIONS();

        } else if (RestMethod.POST.equals(method)) {
            return new POST(chunked);

        } else if (RestMethod.PUT.equals(method)) {
            return new PUT(chunked);

        } else if (RestMethod.DELETE.equals(method)) {
            return new DELETE(chunked);

        } else {
            throw new IllegalArgumentException(String.format("No strategy available for method '%s'", method));
        }
    }
}
