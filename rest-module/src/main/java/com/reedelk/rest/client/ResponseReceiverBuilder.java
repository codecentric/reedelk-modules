package com.reedelk.rest.client;

import com.reedelk.rest.configuration.HttpProtocol;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.exception.ESBException;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiConsumer;

import static com.reedelk.rest.commons.StringUtils.isBlank;
import static com.reedelk.rest.commons.StringUtils.isNotBlank;
import static java.util.Objects.requireNonNull;

public class ResponseReceiverBuilder {

    private Integer port;
    private String host;
    private String basePath;
    private RestMethod method;
    private Boolean keepAlive;
    private HttpProtocol protocol;
    private Boolean followRedirects;
    private BiConsumer<HttpClientRequest, Connection> onRequestHandler;

    private ResponseReceiverBuilder() {
    }

    public static ResponseReceiverBuilder get() {
        return new ResponseReceiverBuilder();
    }

    public ResponseReceiverBuilder host(String host) {
        this.host = requireNonNull(host, "host");
        return this;
    }

    public ResponseReceiverBuilder port(Integer port) {
        this.port = port;
        return this;
    }

    public ResponseReceiverBuilder keepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public ResponseReceiverBuilder basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public ResponseReceiverBuilder followRedirects(Boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public ResponseReceiverBuilder onRequestConsumer(BiConsumer<HttpClientRequest, Connection> onRequestHandler) {
        this.onRequestHandler = onRequestHandler;
        return this;
    }

    public ResponseReceiverBuilder protocol(HttpProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public ResponseReceiverBuilder method(RestMethod method) {
        this.method = method;
        return this;
    }

    public Client build() {
        HttpClient client = HttpClient.create();

        if (keepAlive != null && keepAlive) {
            client = client.keepAlive(keepAlive);
        }
        if (onRequestHandler != null) {
            client = client.doOnRequest(onRequestHandler);
        }
        if (port != null) {
            client = client.port(port);
        }

        String baseUrl = buildBaseUrl();

        client = client.baseUrl(baseUrl);

        Client clientWrapper = new Client(method.addForClient(client));
        clientWrapper.followRedirects(followRedirects);
        clientWrapper.baseUrl(baseUrl);
        return clientWrapper;
    }

    private String buildBaseUrl() {
        String realHost = this.host;
        if (this.host.startsWith("http")) {
            realHost = getHost(this.host);
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
}
