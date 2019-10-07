package com.reedelk.rest.client;

import com.reedelk.rest.configuration.client.ClientConfiguration;

public interface HttpClientService {

    HttpClient clientByConfig(ClientConfiguration id);

    HttpClient clientByBaseURL(String id);

}
