package com.reedelk.rest.client;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.commons.StringUtils;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.http.client.HttpClientRequest;

import java.util.function.BiConsumer;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static io.netty.channel.ChannelOption.SO_RCVBUF;

public class ResponseReceiverBuilder {

    private RestMethod method;
    private HttpClient client;

    private ResponseReceiverBuilder() {
        this.client = HttpClient.create();
    }

    public static ResponseReceiverBuilder get() {
        return new ResponseReceiverBuilder();
    }

    public ResponseReceiverBuilder host(String host) {
        client = client.tcpConfiguration(tcpClient -> tcpClient.host(host));
        return this;
    }

    public ResponseReceiverBuilder port(int port) {
        client = client.tcpConfiguration(tcpClient -> tcpClient.port(port));
        return this;
    }

    public ResponseReceiverBuilder connectionIdleTimeout(int timeoutMillis) {
        client = client.tcpConfiguration(tcpClient ->
                tcpClient.option(CONNECT_TIMEOUT_MILLIS, timeoutMillis));
        return this;
    }

    public ResponseReceiverBuilder responseBufferSize(int responseBufferSize) {
        client = client.tcpConfiguration(tcpClient ->
                tcpClient.option(SO_RCVBUF, responseBufferSize));
        return this;
    }

    public ResponseReceiverBuilder keepAlive(boolean keepAlive) {
        client = client.keepAlive(keepAlive);
        return this;
    }

    public ResponseReceiverBuilder baseUrl(String baseUrl) {
        if (StringUtils.isNotBlank(baseUrl)) {
            client = client.baseUrl(baseUrl);
        }
        return this;
    }

    public ResponseReceiverBuilder followRedirects(Boolean followRedirects) {
        if (followRedirects != null && followRedirects) {
            client = client.followRedirect(true);
        }
        return this;
    }

    public ResponseReceiverBuilder onRequestConsumer(BiConsumer<HttpClientRequest, Connection> onRequestHandler) {
        client = client.doOnRequest(onRequestHandler);
        return this;
    }

    public ResponseReceiverBuilder method(RestMethod method) {
        this.method = method;
        return this;
    }

    public ResponseReceiver<?> build() {
        return method.addForClient(client);
    }
}
