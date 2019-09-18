package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

public class HEADStrategy extends AbstractStrategy {

    @Override
    protected HttpRequestBase baseRequest(BodyProvider bodyProvider) {
        return new HttpHead();
    }
}
