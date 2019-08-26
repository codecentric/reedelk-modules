package com.reedelk.rest.client;

import com.reedelk.rest.configuration.RestMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class HttpClientWrapper {

    private String baseUrl;
    private boolean followRedirects = true;

    private RestMethod method;

    private HttpClient client;
    private HttpClient.ResponseReceiver<?> receiver;

    HttpClientWrapper() {
        this.client = HttpClient.create();
    }

    /**
    void proxy(int proxy) {
        client = client.tcpConfiguration(new Function<TcpClient, TcpClient>() {
            @Override
            public TcpClient apply(TcpClient tcpClient) {
                return tcpClient.proxy(new Consumer<ProxyProvider.TypeSpec>() {
                    @Override
                    public void accept(ProxyProvider.TypeSpec typeSpec) {
                        typeSpec.type(ProxyProvider.Proxy.HTTP)
                                .host("localhost")
                                .port(123)
                                .username("user")
                                .password(new Function<String, String>() {
                                    @Override
                                    public String apply(String s) {
                                        return "sadf";
                                    }
                                });
                    }
                });
            }
        });
    }*/

    void port(int port) {
        client = client.port(port);
    }

    void baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    void method(RestMethod method) {
        this.method = method;
    }

    void keepAlive(boolean keepAlive) {
        client = client.keepAlive(keepAlive);
    }

    void followRedirects(boolean followRedirects) {
        client = client.followRedirect(followRedirects);
    }

    void doOnRequest(BiConsumer<HttpClientRequest, Connection> handler) {
        client = client.doOnRequest(handler);
    }

    void initialize() {
        receiver = method.addForClient(client);
    }

    public Flux<byte[]> execute(String uri, BiFunction<HttpClientResponse, ByteBufFlux, Publisher<byte[]>> handler) {
        return executeInternal(baseUrl + uri, handler);
    }

    private Flux<byte[]> executeInternal(String uri, BiFunction<HttpClientResponse, ByteBufFlux, Publisher<byte[]>> handler) {
        return receiver.uri(uri).response((response, byteBufFlux) -> {
            if (followRedirects && isRedirect(response)) {
                return handleRedirect(handler, response);
            } else {
                return handler.apply(response, byteBufFlux);
            }
        });
    }

    private Publisher<byte[]> handleRedirect(
            BiFunction<HttpClientResponse, ByteBufFlux, Publisher<byte[]>> handler,
            HttpClientResponse response) {

        String redirectUrl = getLocationHeader(response);
        // Absolute
        if (redirectUrl.startsWith("http")) {
            return executeInternal(redirectUrl, handler);
            // Location is relative
        } else {
            return executeInternal(baseUrl + redirectUrl, handler);
        }
    }

    private String getLocationHeader(HttpClientResponse response) {
        return response.responseHeaders().get("Location");
    }

    private boolean isRedirect(HttpClientResponse response) {
        return response.status() == HttpResponseStatus.MOVED_PERMANENTLY ||
                response.status() == HttpResponseStatus.FOUND ||
                response.status() == HttpResponseStatus.SEE_OTHER;
    }
}
