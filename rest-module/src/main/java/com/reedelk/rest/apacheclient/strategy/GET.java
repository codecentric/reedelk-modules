package com.reedelk.rest.apacheclient.strategy;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

public class GET extends BaseStrategy {

    @Override
    protected HttpRequestBase request() {
        return new HttpGet();
    }
}
