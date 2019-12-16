package com.reedelk.rest.client.strategy;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

    final static String METHOD_NAME = "DELETE";

    HttpDeleteWithBody() {
        super();
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
