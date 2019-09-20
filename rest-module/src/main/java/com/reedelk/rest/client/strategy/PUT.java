package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.body.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;

public class PUT extends BaseStrategyWithBody {

    PUT(boolean chunked) {
        super(chunked);
    }

    @Override
    protected HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider) {
        return new HttpPut();
    }
}
