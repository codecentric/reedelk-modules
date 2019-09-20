package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.body.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;

public class POST extends BaseStrategyWithBody {

    POST(boolean chunked) {
        super(chunked);
    }

    @Override
    protected HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider) {
        return new HttpPost();
    }
}
