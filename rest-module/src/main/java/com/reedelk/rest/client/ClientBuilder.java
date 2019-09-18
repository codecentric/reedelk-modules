package com.reedelk.rest.client;

import com.reedelk.rest.commons.BaseUrl;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientRequest;

import java.util.function.BiConsumer;

import static com.reedelk.rest.commons.Preconditions.requireNotNull;
import static com.reedelk.rest.commons.Preconditions.requireTrue;
import static com.reedelk.rest.commons.Predicates.IS_VALID_URL;

public class ClientBuilder {

    private String baseUrl; // only used if useConfiguration == false; (domain + base path)
    private boolean useConfiguration;

    private RestMethod method;
    private ClientConfiguration configuration; // only used if useConfiguration == true;
    private BiConsumer<HttpClientRequest, Connection> onRequestHandler;

    private ClientBuilder() {
    }

    public static ClientBuilder get() {
        return new ClientBuilder();
    }

    public ClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ClientBuilder method(RestMethod method) {
        this.method = method;
        return this;
    }

    public ClientBuilder useConfiguration(boolean useConfiguration) {
        this.useConfiguration = useConfiguration;
        return this;
    }

    public ClientBuilder configuration(ClientConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public ClientBuilder onRequestConsumer(BiConsumer<HttpClientRequest, Connection> onRequestHandler) {
        this.onRequestHandler = onRequestHandler;
        return this;
    }

    public HttpClientWrapper build() {
        return useConfiguration ?
                buildWithConfig() :
                buildWithBaseURL();
    }

    private HttpClientWrapper buildWithConfig() {
        requireNotNull(configuration, "Configuration must not be null");
        requireNotNull(method, "HTTP method must not be null");

        HttpClientWrapper client = newWrapper();

        Integer port = configuration.getPort();
        if (port != null) {
            client.port(port);
        }

        Boolean keepAlive = configuration.getKeepAlive();
        if (keepAlive != null) {
            client.keepAlive(keepAlive);
        }

        Boolean followRedirects = configuration.getFollowRedirects();
        if (followRedirects != null) {
            client.followRedirects(followRedirects);
        }

        Boolean contentCompression = configuration.getContentCompression();
        if (contentCompression != null) {
            client.compress(contentCompression);
        }

        if (onRequestHandler != null) {
            client.doOnRequest(onRequestHandler);
        }

        String baseUrl = BaseUrl.from(configuration);
        client.baseURL(baseUrl);
        client.method(method);
        client.initialize();
        return client;
    }

    private HttpClientWrapper buildWithBaseURL() {
        requireNotNull(baseUrl, "Base URL must not to be null");
        requireNotNull(method, "HTTP method must not be null");

        if (!baseUrl.startsWith("http")) {
            // By default we prefix the base url with HTTP (default protocol) if it is missing
            baseUrl = HttpProtocol.HTTP.name().toLowerCase() + "://" + baseUrl;
        }

        requireTrue(IS_VALID_URL, baseUrl, "Base URL is not a valid URL");

        HttpClientWrapper client = newWrapper();

        if (onRequestHandler != null) {
            client.doOnRequest(onRequestHandler);
        }

        client.baseURL(baseUrl);
        client.method(method);
        client.initialize();
        return client;
    }

    HttpClientWrapper newWrapper() {
        return new HttpClientWrapper();
    }

}
