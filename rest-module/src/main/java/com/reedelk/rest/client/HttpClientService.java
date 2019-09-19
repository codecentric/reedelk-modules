package com.reedelk.rest.client;

import com.reedelk.rest.configuration.client.ClientConfiguration;
import org.apache.http.nio.client.HttpAsyncClient;

public interface HttpClientService {

    HttpAsyncClient clientByConfig(ClientConfiguration id);

    HttpAsyncClient clientByBaseURL(String id);

    void dispose();
}
