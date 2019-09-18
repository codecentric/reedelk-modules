package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpRequestBase;

public class OPTIONStrategy extends AbstractStrategy {

    @Override
    protected HttpRequestBase baseRequest(BodyProvider bodyProvider) {
        return new HttpOptions();
    }
}
