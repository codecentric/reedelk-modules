package com.reedelk.rest.client.strategy;

import org.apache.http.client.methods.HttpRequestBase;

public interface RequestWithoutBodyFactory {
    HttpRequestBase create();
}
