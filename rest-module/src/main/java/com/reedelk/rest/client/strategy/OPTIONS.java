package com.reedelk.rest.client.strategy;

import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpRequestBase;

public class OPTIONS extends BaseStrategy {

    @Override
    protected HttpRequestBase request() {
        return new HttpOptions();
    }
}