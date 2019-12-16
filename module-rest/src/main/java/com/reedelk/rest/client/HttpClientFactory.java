package com.reedelk.rest.client;

import com.reedelk.rest.configuration.client.ClientConfiguration;

public interface HttpClientFactory {

    HttpClient from(ClientConfiguration config);

    HttpClient from(String baseURL);
}
