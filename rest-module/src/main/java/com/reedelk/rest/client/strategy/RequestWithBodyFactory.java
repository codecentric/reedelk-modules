package com.reedelk.rest.client.strategy;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public interface RequestWithBodyFactory {
    HttpEntityEnclosingRequestBase create();
}
