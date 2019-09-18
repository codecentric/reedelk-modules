package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.apacheclient.BodyProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;

public class PUTStrategy extends AbstractStrategy {

    @Override
    protected HttpRequestBase baseRequest(BodyProvider bodyProvider) {
        HttpPut put = new HttpPut();
        put.setEntity(new BasicHttpEntity());
        return put;
    }
}
