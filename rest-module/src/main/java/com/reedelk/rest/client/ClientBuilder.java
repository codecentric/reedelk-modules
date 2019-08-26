package com.reedelk.rest.client;

import com.reedelk.rest.configuration.HttpProtocol;
import com.reedelk.rest.configuration.RestClientConfiguration;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.exception.ESBException;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiConsumer;

import static com.reedelk.rest.commons.Preconditions.*;
import static com.reedelk.rest.commons.Predicates.IS_VALID_URL;
import static com.reedelk.rest.commons.StringUtils.isBlank;
import static com.reedelk.rest.commons.StringUtils.isNotBlank;

public class ClientBuilder {

    private String baseUrl; // only used if useConfiguration == false; (domain + base path)
    private boolean useConfiguration;

    private RestMethod method;
    private RestClientConfiguration configuration; // only used if useConfiguration == true;
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

    public ClientBuilder configuration(RestClientConfiguration configuration) {
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

        if (onRequestHandler != null) {
            client.doOnRequest(onRequestHandler);
        }

        String baseUrl = baseUrlFrom(configuration);
        client.baseUrl(baseUrl);
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

        client.baseUrl(baseUrl);
        client.method(method);
        client.initialize();
        return client;
    }


    private String baseUrlFrom(RestClientConfiguration configuration) {
        String basePath = configuration.getBasePath();
        String host = requireNotBlank(configuration.getHost(), "'Host' must not be empty");
        HttpProtocol protocol = requireNotNull(configuration.getProtocol(), "'Protocol' must not be null");

        String realHost = host;
        if (host.startsWith("http")) {
            realHost = getHost(host);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(protocol.name().toLowerCase())
                .append("://")
                .append(realHost);
        if (isNotBlank(basePath)) {
            builder.append(basePath);
        }
        return builder.toString();
    }

    private String getHost(String host) {
        try {
            URI uri = new URI(host);
            String realHost = uri.getHost();
            if (isBlank(realHost)) {
                throw new ESBException(String.format("Could not extract host from [%s]", host));
            }
            return realHost;
        } catch (URISyntaxException e) {
            throw new ESBException(e);
        }
    }

    HttpClientWrapper newWrapper() {
        return new HttpClientWrapper();
    }
}
