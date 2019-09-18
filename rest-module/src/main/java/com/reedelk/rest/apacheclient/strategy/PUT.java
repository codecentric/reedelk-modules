package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;

public class PUT extends BaseStrategyWithBody {

    @Override
    protected HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider) {
        return new HttpPut();
    }
}
