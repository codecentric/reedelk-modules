package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

public class GETStrategy extends AbstractStrategy {

    @Override
    protected HttpRequestBase baseRequest(BodyProvider bodyProvider) {
        return new HttpGet();
    }
}
