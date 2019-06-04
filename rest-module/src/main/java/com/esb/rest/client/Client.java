package com.esb.rest.client;

import com.esb.rest.commons.RestMethod;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

public class Client {

    private static final OkHttpClient CLIENT = new OkHttpClient();

    private String url;
    private RestMethod method;

    public Client(String url, RestMethod method) {
        this.url = url;
        this.method = method;
    }

    public String make() {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            return CLIENT.newCall(request)
                    .execute()
                    .body()
                    .string();
        } catch (IOException e) {
            throw new RuntimeException("Could not make HTTP call", e);
        }
    }
}
